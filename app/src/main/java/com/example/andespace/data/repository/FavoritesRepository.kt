package com.example.andespace.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.AddFavoriteRequest
import com.example.andespace.model.dto.RoomDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FavoritesRepository(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    private val gson = Gson()
    private val roomListType = object : TypeToken<List<RoomDto>>() {}.type
    private val FAVORITES_JSON_KEY = stringPreferencesKey("favorites_json")
    companion object {
        private const val TAG = "FavoritesRepository"
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
        val prefs = dataStore.data
            .catch { emit(emptyPreferences()) }
            .first()
        val json = prefs[FAVORITES_JSON_KEY]
        return if (!json.isNullOrBlank()) {
            runCatching<List<RoomDto>> { gson.fromJson(json, roomListType) }
                .getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }
    suspend fun saveLocalFavorites(rooms: List<RoomDto>) {
        val json = gson.toJson(rooms)
        dataStore.edit { prefs ->
            prefs[FAVORITES_JSON_KEY] = json
        }
    }

    suspend fun addFavorite(room: RoomDto): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = AddFavoriteRequest(
                roomId = room.id,
                name = room.name,
                building = room.building,
                buildingCode = room.buildingCode,
                capacity = room.capacity,
                utilities = room.utilities
            )

            Log.d(TAG, "addFavorite -> roomId=${room.id}")

            val response = apiService.addFavorite(request)
            Log.d(
                TAG,
                "addFavorite response code=${response.code()}, successful=${response.isSuccessful}"
            )
            if (response.isSuccessful || response.code() == 201) {
                Result.success(true)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Log.e(TAG, "addFavorite failed: $msg")
                Result.failure(ApiException(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "addFavorite exception: ${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun deleteFavorite(roomId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "deleteFavorite -> roomId=$roomId")
            val response = apiService.deleteFavorite(roomId)
            Log.d(
                TAG,
                "deleteFavorite response code=${response.code()}, successful=${response.isSuccessful}"
            )
            if (response.isSuccessful || response.code() == 204) {
                Result.success(true)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Log.e(TAG, "deleteFavorite failed: $msg")
                Result.failure(ApiException(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteFavorite exception: ${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }
}