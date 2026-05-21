package com.example.andespace.data.repository

import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.model.dto.AcceptFriendshipRequest
import com.example.andespace.model.dto.CreateFriendshipRequest
import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.WeeklyScheduleOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendsRepository(
    private val apiService: ApiService
) {
    suspend fun getMyFriends(): Result<List<FriendItemOut>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyFriends()
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Result.failure(ApiException("Error fetching friends: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection or server error."))
        }
    }

    suspend fun getFriendWeeklySchedule(email: String, date: String): Result<WeeklyScheduleOut> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFriendWeeklySchedule(targetEmail = email, date = date)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty schedule body"))
                }
            } else {
                Result.failure(ApiException("Error fetching friend schedule: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection or server error."))
        }
    }

    suspend fun sendFriendRequest(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendFriendRequest(CreateFriendshipRequest(correo_amigo_2 = email))
            if (response.isSuccessful) {
                Result.success(true)
            } else if (response.code() == 404) {
                Result.failure(ApiException("This user does not exist"))
            } else {
                Result.failure(ApiException("Error sending request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection or server error."))
        }
    }

    suspend fun getIncomingRequests(): Result<List<FriendItemOut>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getIncomingRequests()
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Result.failure(ApiException("Error fetching requests: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection or server error."))
        }
    }

    suspend fun acceptFriendRequest(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptFriendRequest(AcceptFriendshipRequest(correo_amigo_1 = email))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(ApiException("Error accepting request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection or server error."))
        }
    }
}