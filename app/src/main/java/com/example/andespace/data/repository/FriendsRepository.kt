package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.AddFriendRequest
import com.example.andespace.model.dto.FriendUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendsRepository(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "FriendsRepository"
    }

    suspend fun getMyFriends(): Result<List<FriendUiModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyFriends()
            if (response.isSuccessful) {
                val friends = response.body()?.items.orEmpty().mapNotNull { it.toUiModel() }
                Log.d(TAG, "getMyFriends -> ${friends.size} friends")
                Result.success(friends)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Log.e(TAG, "getMyFriends failed: $msg")
                Result.failure(ApiException(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMyFriends exception: ${e.message}", e)
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun addFriend(username: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addFriend(AddFriendRequest(username = username.trim()))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

    suspend fun deleteFriend(username: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteFriend(username)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }
}
