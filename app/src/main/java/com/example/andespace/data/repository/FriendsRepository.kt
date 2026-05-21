package com.example.andespace.data.repository

import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.AcceptFriendshipRequest
import com.example.andespace.model.dto.CreateFriendshipRequest
import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.MyFriendsResponse
import com.example.andespace.model.dto.WeeklyScheduleOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class FriendsRepository(
    private val apiService: ApiService
) {
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
        unitRequest { apiService.sendFriendRequest(CreateFriendshipRequest(correo_amigo_2 = email.trim())) }
    }

    suspend fun acceptFriendRequest(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        unitRequest { apiService.acceptFriendRequest(AcceptFriendshipRequest(correo_amigo_1 = email)) }
    }

    suspend fun deleteFriendship(friendEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteFriendship(friendEmail)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
        } catch (_: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }

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

    private suspend fun unitRequest(call: suspend () -> Response<Any>): Result<Boolean> {
        return try {
            val response = call()
            if (response.isSuccessful) Result.success(true)
            else Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
        } catch (_: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
    }
}
