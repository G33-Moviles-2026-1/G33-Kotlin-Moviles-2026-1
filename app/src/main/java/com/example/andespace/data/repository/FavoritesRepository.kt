package com.example.andespace.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.model.db.favorites.FavoritesDao
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.network.dataStore
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.db.favorites.FavoriteRoomEntity
import com.example.andespace.model.db.sync.PendingSyncAction
import com.example.andespace.model.db.sync.SyncActionDao
import com.example.andespace.model.dto.AddFavoriteRequest
import com.example.andespace.model.dto.RoomDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class FavoritesRepository(
    private val apiService: ApiService,
    private val favoritesDao: FavoritesDao,
    private val syncActionDao: SyncActionDao,
    private val context: Context
) {
    private val gson = Gson()
    private val utilityListType = object : TypeToken<List<String>>() {}.type
    private val syncMutex = Mutex()
    companion object {
        private const val TAG = "FavoritesRepository"
        private const val ANONYMOUS_USER_KEY = "anonymous_user"
        private const val ACTION_ADD_FAVORITE = "ADD_FAVORITE"
        private const val ACTION_DELETE_FAVORITE = "DELETE_FAVORITE"
    }

    suspend fun getMyFavorites(): Result<List<RoomDto>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "getMyFavorites -> fetching from backend")
                val response = apiService.getMyFavorites()
                Log.d(TAG, "getMyFavorites code=${response.code()}, ok=${response.isSuccessful}")
                if (response.isSuccessful) {
                    val items = response.body()?.items ?: emptyList()
                    val rooms = items.mapNotNull { it.toRoomDto() }
                    Log.d(
                        TAG,
                        "getMyFavorites -> parsed ${rooms.size} rooms: ${rooms.map { it.id }}"
                    )
                    Result.success(rooms)
                } else {
                    val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                    Log.e(TAG, "getMyFavorites failed: $msg")
                    Result.failure(ApiException(msg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "getMyFavorites exception: ${e.message}", e)
                Result.failure(Exception("No internet connection. Please check your network and try again."))
            }
        }
    }

    suspend fun getLocalFavorites(): List<RoomDto> {
        val userKey = getCurrentUserKey()
        return favoritesDao.getFavoritesByUser(userKey).map { it.toRoomDto() }
    }

    suspend fun saveLocalFavorites(rooms: List<RoomDto>) {
        val userKey = getCurrentUserKey()
        favoritesDao.clearFavoritesForUser(userKey)
        rooms.forEach { room ->
            favoritesDao.insertFavorite(room.toEntity(userKey))
        }
    }

    suspend fun addFavorite(room: RoomDto): Result<Boolean> = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        val request = AddFavoriteRequest(
            roomId = room.id,
            name = room.name,
            building = room.building,
            buildingCode = room.buildingCode,
            capacity = room.capacity,
            utilities = room.utilities
        )
        val mutationKey = mutationKey(userKey, room.id)

        try {
            Log.d(TAG, "addFavorite -> roomId=${room.id}")
            favoritesDao.insertFavorite(room.toEntity(userKey))

            if (!NetworkMonitor.isOnline.value) {
                enqueueFavoriteAction(
                    mutationKey = mutationKey,
                    actionType = ACTION_ADD_FAVORITE,
                    payload = gson.toJson(request)
                )
                syncPendingFavoriteActions()
                Result.success(true)
            } else {
                val response = apiService.addFavorite(request)
                Log.d(
                    TAG,
                    "addFavorite response code=${response.code()}, successful=${response.isSuccessful}"
                )
                if (response.isSuccessful || response.code() == 201) {
                    Result.success(true)
                } else {
                    val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                    Log.e(TAG, "addFavorite failed, enqueued: $msg")
                    enqueueFavoriteAction(
                        mutationKey = mutationKey,
                        actionType = ACTION_ADD_FAVORITE,
                        payload = gson.toJson(request)
                    )
                    syncPendingFavoriteActions()
                    Result.success(true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "addFavorite exception, enqueued: ${e.message}", e)
            enqueueFavoriteAction(
                mutationKey = mutationKey,
                actionType = ACTION_ADD_FAVORITE,
                payload = gson.toJson(request)
            )
            syncPendingFavoriteActions()
            Result.success(true)
        }
    }

    suspend fun deleteFavorite(roomId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        val mutationKey = mutationKey(userKey, roomId)

        try {
            Log.d(TAG, "deleteFavorite -> roomId=$roomId")
            favoritesDao.deleteFavoriteByRoomId(userKey, roomId)

            if (!NetworkMonitor.isOnline.value) {
                enqueueFavoriteAction(
                    mutationKey = mutationKey,
                    actionType = ACTION_DELETE_FAVORITE,
                    payload = roomId
                )
                syncPendingFavoriteActions()
                Result.success(true)
            } else {
                val response = apiService.deleteFavorite(roomId)
                Log.d(
                    TAG,
                    "deleteFavorite response code=${response.code()}, successful=${response.isSuccessful}"
                )
                if (response.isSuccessful || response.code() == 204) {
                    Result.success(true)
                } else {
                    val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                    Log.e(TAG, "deleteFavorite failed, enqueued: $msg")
                    enqueueFavoriteAction(
                        mutationKey = mutationKey,
                        actionType = ACTION_DELETE_FAVORITE,
                        payload = roomId
                    )
                    syncPendingFavoriteActions()
                    Result.success(true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteFavorite exception, enqueued: ${e.message}", e)
            enqueueFavoriteAction(
                mutationKey = mutationKey,
                actionType = ACTION_DELETE_FAVORITE,
                payload = roomId
            )
            syncPendingFavoriteActions()
            Result.success(true)
        }
    }

    suspend fun syncPendingFavoriteActions() = withContext(Dispatchers.IO) {
        if (!NetworkMonitor.isOnline.value) return@withContext

        syncMutex.withLock {
            val pending = syncActionDao.getPendingFavoriteActions()
            if (pending.isEmpty()) return@withLock

            for (action in pending) {
                try {
                    when (action.actionType) {
                        ACTION_ADD_FAVORITE -> {
                            val request = gson.fromJson(action.payload, AddFavoriteRequest::class.java)
                            val response = apiService.addFavorite(request)
                            if (!response.isSuccessful && response.code() != 201) {
                                break
                            }
                        }
                        ACTION_DELETE_FAVORITE -> {
                            val response = apiService.deleteFavorite(action.payload)
                            if (!response.isSuccessful && response.code() != 204) {
                                break
                            }
                        }
                    }
                    syncActionDao.deleteAction(action.id)
                } catch (_: IOException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Dropping invalid queued favorite action id=${action.id}", e)
                    syncActionDao.deleteAction(action.id)
                }
            }
        }
    }

    private suspend fun getCurrentUserKey(): String {
        val cookieKey = stringPreferencesKey("session_cookie")
        val sessionCookie = context.dataStore.data.first()[cookieKey].orEmpty()
        return sessionCookie.ifBlank { ANONYMOUS_USER_KEY }
    }

    private suspend fun enqueueFavoriteAction(
        mutationKey: String,
        actionType: String,
        payload: String
    ) {
        syncActionDao.deleteFavoriteActionsByMutationKey(mutationKey)
        syncActionDao.insertAction(
            PendingSyncAction(
                actionType = actionType,
                payload = payload,
                localClassId = mutationKey
            )
        )
    }

    private fun mutationKey(userKey: String, roomId: String): String {
        return "$userKey::$roomId"
    }

    private fun FavoriteRoomEntity.toRoomDto(): RoomDto {
        val utilities = runCatching<List<String>> {
            gson.fromJson(utilitiesJson, utilityListType)
        }.getOrDefault(emptyList())

        return RoomDto(
            id = roomId,
            name = name,
            building = building,
            buildingCode = buildingCode,
            capacity = capacity,
            utilities = utilities,
            waitSeconds = null,
            matchingWindows = emptyList()
        )
    }

    private fun RoomDto.toEntity(userKey: String): FavoriteRoomEntity {
        return FavoriteRoomEntity(
            userKey = userKey,
            roomId = id,
            name = name,
            building = building,
            buildingCode = buildingCode,
            capacity = capacity,
            utilitiesJson = gson.toJson(utilities)
        )
    }
}