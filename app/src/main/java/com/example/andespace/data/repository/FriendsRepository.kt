package com.example.andespace.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.network.dataStore
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.db.friends.CachedFriendEntity
import com.example.andespace.model.db.friends.FriendCacheRole
import com.example.andespace.model.db.friends.FriendsDao
import com.example.andespace.model.db.friends.FriendsUiDraftEntity
import com.example.andespace.model.db.sync.PendingSyncAction
import com.example.andespace.model.db.sync.SyncActionDao
import com.example.andespace.model.dto.AcceptFriendshipRequest
import com.example.andespace.model.dto.ChangeStatusRequest
import com.example.andespace.model.dto.CreateFriendshipRequest
import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.MyFriendsResponse
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.model.dto.WeeklyScheduleOut
import com.example.andespace.ui.friends.OutgoingFriendRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

class FriendsRepository(
    private val apiService: ApiService,
    private val friendsDao: FriendsDao,
    private val syncActionDao: SyncActionDao,
    private val context: Context,
    private val gson: Gson,
    private val accountRepository: AccountRepository
) {
    private val syncMutex = Mutex()

    companion object {
        private const val TAG = "FriendsRepository"
        private const val ANONYMOUS_USER_KEY = "anonymous_user"
        private const val ACTION_SEND_FRIEND_REQUEST = "SEND_FRIEND_REQUEST"
        private const val ACTION_ACCEPT_FRIEND_REQUEST = "ACCEPT_FRIEND_REQUEST"
        private const val ACTION_DELETE_FRIENDSHIP = "DELETE_FRIENDSHIP"
        private const val ACTION_CHANGE_MY_STATUS = "CHANGE_MY_STATUS"
    }

    suspend fun loadLocalSnapshot(): FriendsLocalSnapshot = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        val friends = friendsDao.getFriendsByRole(userKey, FriendCacheRole.ACCEPTED).map { it.toFriendItemOut() }
        val incoming = friendsDao.getFriendsByRole(userKey, FriendCacheRole.INCOMING).map { it.toFriendItemOut() }
        val outgoing = friendsDao.getFriendsByRole(userKey, FriendCacheRole.OUTGOING).map { it.toOutgoingRequest() }
        val rawSuggestions = friendsDao.getFriendsByRole(userKey, FriendCacheRole.SUGGESTION).map { it.username }
        val searchDraft = friendsDao.getSearchDraft(userKey).orEmpty()
        val myStatus = accountRepository.loadCachedProfile().status
        FriendsLocalSnapshot(
            friends = friends,
            incoming = incoming,
            outgoing = outgoing,
            suggestions = filterVisibleSuggestions(rawSuggestions, friends, incoming, outgoing),
            searchDraft = searchDraft,
            myStatus = myStatus
        )
    }

    suspend fun saveSearchDraft(query: String) = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        friendsDao.upsertSearchDraft(FriendsUiDraftEntity(userKey = userKey, searchQuery = query))
    }

    suspend fun getSearchDraft(): String = withContext(Dispatchers.IO) {
        friendsDao.getSearchDraft(getCurrentUserKey()).orEmpty()
    }

    suspend fun refreshAllParallel(): Result<FriendsNetworkBundle> = withContext(Dispatchers.IO) {
        if (!NetworkMonitor.isOnline.value) {
            return@withContext Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
        coroutineScope {
            val friendsDeferred = async { getMyFriends() }
            val incomingDeferred = async { getIncomingRequests() }
            val suggestionsDeferred = async { getSuggestions() }

            val friendsResult = friendsDeferred.await()
            val incomingResult = incomingDeferred.await()
            val suggestionsResult = suggestionsDeferred.await()

            if (friendsResult.isFailure && incomingResult.isFailure && suggestionsResult.isFailure) {
                return@coroutineScope Result.failure(
                    friendsResult.exceptionOrNull()
                        ?: incomingResult.exceptionOrNull()
                        ?: suggestionsResult.exceptionOrNull()
                        ?: Exception(RepositoryMessages.NO_INTERNET)
                )
            }

            Result.success(
                FriendsNetworkBundle(
                    friends = friendsResult.getOrElse { emptyList() },
                    incoming = incomingResult.getOrElse { emptyList() },
                    suggestions = suggestionsResult.getOrNull()
                )
            )
        }
    }

    suspend fun persistNetworkBundle(bundle: FriendsNetworkBundle) = withContext(Dispatchers.IO) {
        saveFriendsByRole(bundle.friends, FriendCacheRole.ACCEPTED)
        saveFriendsByRole(bundle.incoming, FriendCacheRole.INCOMING)
        bundle.suggestions?.let { saveSuggestions(it) }
    }

    suspend fun getMyFriends(): Result<List<FriendItemOut>> = withContext(Dispatchers.IO) {
        friendsListRequest { apiService.getMyFriends() }
    }

    suspend fun getIncomingRequests(): Result<List<FriendItemOut>> = withContext(Dispatchers.IO) {
        friendsListRequest { apiService.getIncomingRequests() }
    }

    suspend fun getSuggestions(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFriendSuggestions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (_: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun getFriendWeeklySchedule(email: String, date: String): Result<WeeklyScheduleOut> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFriendWeeklySchedule(targetEmail = email, date = date)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) Result.success(body)
                    else Result.failure(ApiException(extractErrorMessage(null, response.code())))
                } else {
                    Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
                }
            } catch (_: Exception) {
                Result.failure(Exception(RepositoryMessages.NO_INTERNET))
            }
        }

    suspend fun sendFriendRequest(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val normalized = usernameToEmail(email)
        val userKey = getCurrentUserKey()
        val username = normalized.substringBefore("@")
        val request = CreateFriendshipRequest(correo_amigo_2 = normalized)
        val mutationKey = friendMutationKey(userKey, normalized, ACTION_SEND_FRIEND_REQUEST)

        friendsDao.deleteByEmail(userKey, normalized)
        cacheOutgoingFriend(userKey, normalized, username)
        saveSearchDraft("")

        if (!NetworkMonitor.isOnline.value) {
            enqueueFriendAction(mutationKey, ACTION_SEND_FRIEND_REQUEST, gson.toJson(request))
            syncPendingFriendActions()
            return@withContext Result.success(true)
        }

        try {
            val response = apiService.sendFriendRequest(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                enqueueFriendAction(mutationKey, ACTION_SEND_FRIEND_REQUEST, gson.toJson(request))
                syncPendingFriendActions()
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendFriendRequest exception, enqueued: ${e.message}", e)
            enqueueFriendAction(mutationKey, ACTION_SEND_FRIEND_REQUEST, gson.toJson(request))
            syncPendingFriendActions()
            Result.success(true)
        }
    }

    suspend fun acceptFriendRequest(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        val request = AcceptFriendshipRequest(correo_amigo_1 = email)
        val mutationKey = friendMutationKey(userKey, email, ACTION_ACCEPT_FRIEND_REQUEST)

        moveIncomingToAcceptedLocally(userKey, email)

        if (!NetworkMonitor.isOnline.value) {
            enqueueFriendAction(mutationKey, ACTION_ACCEPT_FRIEND_REQUEST, gson.toJson(request))
            syncPendingFriendActions()
            return@withContext Result.success(true)
        }

        try {
            val response = apiService.acceptFriendRequest(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                enqueueFriendAction(mutationKey, ACTION_ACCEPT_FRIEND_REQUEST, gson.toJson(request))
                syncPendingFriendActions()
                Result.success(true)
            }
        } catch (e: Exception) {
            enqueueFriendAction(mutationKey, ACTION_ACCEPT_FRIEND_REQUEST, gson.toJson(request))
            syncPendingFriendActions()
            Result.success(true)
        }
    }

    suspend fun deleteFriendship(friendEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        val mutationKey = friendMutationKey(userKey, friendEmail, ACTION_DELETE_FRIENDSHIP)

        friendsDao.deleteByEmail(userKey, friendEmail)

        if (!NetworkMonitor.isOnline.value) {
            enqueueFriendAction(mutationKey, ACTION_DELETE_FRIENDSHIP, friendEmail)
            syncPendingFriendActions()
            return@withContext Result.success(Unit)
        }

        try {
            val response = apiService.deleteFriendship(friendEmail)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                enqueueFriendAction(mutationKey, ACTION_DELETE_FRIENDSHIP, friendEmail)
                syncPendingFriendActions()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            enqueueFriendAction(mutationKey, ACTION_DELETE_FRIENDSHIP, friendEmail)
            syncPendingFriendActions()
            Result.success(Unit)
        }
    }

    suspend fun changeMyStatus(status: UserStatus): Result<Unit> = withContext(Dispatchers.IO) {
        val userKey = getCurrentUserKey()
        val request = ChangeStatusRequest(status = status.value)
        val mutationKey = "$userKey::my_status"

        accountRepository.saveStatusLocally(status)

        if (!NetworkMonitor.isOnline.value) {
            enqueueFriendAction(mutationKey, ACTION_CHANGE_MY_STATUS, gson.toJson(request))
            syncPendingFriendActions()
            return@withContext Result.success(Unit)
        }

        try {
            val response = apiService.changeStatus(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                enqueueFriendAction(mutationKey, ACTION_CHANGE_MY_STATUS, gson.toJson(request))
                syncPendingFriendActions()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            enqueueFriendAction(mutationKey, ACTION_CHANGE_MY_STATUS, gson.toJson(request))
            syncPendingFriendActions()
            Result.success(Unit)
        }
    }

    suspend fun syncPendingFriendActions() = withContext(Dispatchers.IO) {
        if (!NetworkMonitor.isOnline.value) return@withContext

        syncMutex.withLock {
            val pending = syncActionDao.getPendingFriendActions()
            if (pending.isEmpty()) return@withLock

            for (action in pending) {
                try {
                    when (action.actionType) {
                        ACTION_SEND_FRIEND_REQUEST -> {
                            val request = gson.fromJson(action.payload, CreateFriendshipRequest::class.java)
                            val response = apiService.sendFriendRequest(request)
                            if (!response.isSuccessful) break
                        }
                        ACTION_ACCEPT_FRIEND_REQUEST -> {
                            val request = gson.fromJson(action.payload, AcceptFriendshipRequest::class.java)
                            val response = apiService.acceptFriendRequest(request)
                            if (!response.isSuccessful) break
                        }
                        ACTION_DELETE_FRIENDSHIP -> {
                            val response = apiService.deleteFriendship(action.payload)
                            if (!response.isSuccessful) break
                        }
                        ACTION_CHANGE_MY_STATUS -> {
                            val request = gson.fromJson(action.payload, ChangeStatusRequest::class.java)
                            val response = apiService.changeStatus(request)
                            if (!response.isSuccessful) break
                        }
                    }
                    syncActionDao.deleteAction(action.id)
                } catch (_: IOException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Dropping invalid queued friend action id=${action.id}", e)
                    syncActionDao.deleteAction(action.id)
                }
            }

            refreshAllParallel().onSuccess { persistNetworkBundle(it) }
        }
    }

    private suspend fun saveFriendsByRole(friends: List<FriendItemOut>, role: String) {
        val userKey = getCurrentUserKey()
        friendsDao.clearByRole(userKey, role)
        if (friends.isNotEmpty()) {
            friendsDao.insertFriends(friends.map { it.toEntity(userKey, role) })
        }
    }

    private suspend fun saveSuggestions(suggestions: List<String>) {
        val userKey = getCurrentUserKey()
        friendsDao.clearByRole(userKey, FriendCacheRole.SUGGESTION)
        if (suggestions.isEmpty()) return
        friendsDao.insertFriends(
            suggestions.map { username ->
                CachedFriendEntity(
                    userKey = userKey,
                    email = usernameToEmail(username),
                    username = username.trim(),
                    status = UserStatus.INCOGNITO.value,
                    role = FriendCacheRole.SUGGESTION
                )
            }
        )
    }

    private fun filterVisibleSuggestions(
        suggestions: List<String>,
        friends: List<FriendItemOut>,
        incoming: List<FriendItemOut>,
        outgoing: List<OutgoingFriendRequest>
    ): List<String> {
        val blocked = mutableSetOf<String>()
        friends.forEach {
            blocked.add(it.username.lowercase())
            blocked.add(it.email.lowercase())
        }
        incoming.forEach {
            blocked.add(it.username.lowercase())
            blocked.add(it.email.lowercase())
        }
        outgoing.forEach {
            blocked.add(it.username.lowercase())
            blocked.add(it.email.lowercase())
        }
        return suggestions
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .filter { username ->
                val normalized = username.lowercase()
                val email = usernameToEmail(username).lowercase()
                normalized !in blocked && email !in blocked
            }
    }

    private fun usernameToEmail(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.contains("@")) trimmed else "$trimmed@uniandes.edu.co"
    }

    private suspend fun cacheOutgoingFriend(userKey: String, email: String, username: String) {
        friendsDao.insertFriends(
            listOf(
                CachedFriendEntity(
                    userKey = userKey,
                    email = email,
                    username = username,
                    status = UserStatus.INCOGNITO.value,
                    role = FriendCacheRole.OUTGOING
                )
            )
        )
    }

    private suspend fun moveIncomingToAcceptedLocally(userKey: String, email: String) {
        val incoming = friendsDao.getFriendsByRole(userKey, FriendCacheRole.INCOMING)
            .firstOrNull { it.email == email } ?: return
        friendsDao.deleteByEmail(userKey, email)
        friendsDao.insertFriends(
            listOf(incoming.copy(role = FriendCacheRole.ACCEPTED))
        )
    }

    private suspend fun enqueueFriendAction(mutationKey: String, actionType: String, payload: String) {
        syncActionDao.deleteFriendActionsByMutationKey(mutationKey)
        syncActionDao.insertAction(
            PendingSyncAction(
                actionType = actionType,
                payload = payload,
                localClassId = mutationKey
            )
        )
    }

    private suspend fun getCurrentUserKey(): String {
        val cookieKey = stringPreferencesKey("session_cookie")
        val sessionCookie = context.dataStore.data.first()[cookieKey].orEmpty()
        return sessionCookie.ifBlank { ANONYMOUS_USER_KEY }
    }

    private fun friendMutationKey(userKey: String, email: String, action: String): String =
        "$userKey::$email::$action"

    private suspend fun friendsListRequest(
        call: suspend () -> Response<MyFriendsResponse>
    ): Result<List<FriendItemOut>> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (_: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    private fun FriendItemOut.toEntity(userKey: String, role: String): CachedFriendEntity =
        CachedFriendEntity(
            userKey = userKey,
            email = email,
            username = username,
            status = status ?: UserStatus.INCOGNITO.value,
            role = role
        )

    private fun CachedFriendEntity.toFriendItemOut(): FriendItemOut =
        FriendItemOut(email = email, username = username, status = status)

    private fun CachedFriendEntity.toOutgoingRequest(): OutgoingFriendRequest =
        OutgoingFriendRequest(email = email, username = username)
}
