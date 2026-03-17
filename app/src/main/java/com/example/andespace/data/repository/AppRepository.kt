package com.example.andespace.data.repository
import android.util.Log
import com.example.andespace.data.model.dto.AnalyticsEventRequest
import com.example.andespace.data.model.dto.RoomSearchRequest
import com.example.andespace.data.model.dto.RoomSearchResponse
import com.example.andespace.data.model.dto.RoomTimeWindowDto
import com.example.andespace.data.model.dto.toTimeWindows
import com.example.andespace.data.model.HomeSearchParams
import com.example.andespace.data.network.LoginRequest
import com.example.andespace.data.network.NetworkModule
import com.example.andespace.data.network.RegisterRequest
import com.example.andespace.data.network.ApiService

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class AppRepository {
    private val apiService: ApiService = NetworkModule.apiService
    private val sessionId = UUID.randomUUID().toString()


    suspend fun register(email: String, password: String, semester: String): Result<Boolean> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, semester))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Check your connection"))

        }
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Check your connection"))
        }
    }

    suspend fun getMeData(): Result<String> {
        return try {
            val response = apiService.checkSession()
            if (response.isSuccessful) {
                val dataString = response.body().toString()
                Result.success(dataString)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Check your connection"))
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            apiService.logout()
            NetworkModule.cookieJar.clearCookies()
            Result.success(true)
        } catch (e: Exception) {
            NetworkModule.cookieJar.clearCookies()
            Result.failure(Exception("Network error: Check your connection"))
        }
    }

    private fun extractErrorMessage(errorBody: String?, defaultCode: Int): String {
        if (errorBody.isNullOrEmpty()) return "Error code: $defaultCode"

        return try {
            val jsonObject = JSONObject(errorBody)
            when {
                jsonObject.has("detail") -> jsonObject.getString("detail")
                jsonObject.has("message") -> jsonObject.getString("message")
                else -> "Error code: $defaultCode"
            }
        } catch (e: Exception) {
            "Error code: $defaultCode"
        }
    }

    suspend fun searchRooms(
        params: HomeSearchParams,
        limit: Int,
        offset: Int
    ): Result<RoomSearchResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = RoomSearchRequest(
                    roomPrefix = params.classroom.ifBlank { null },
                    date = params.date,
                    since = "${params.since}:00",
                    until = "${params.until}:00",
                    utilities = params.utilities,
                    nearMe = params.closeToMe,
                    limit = limit,
                    offset = offset
                )
                Log.d(
                    TAG,
                    "searchRooms request -> roomPrefix=${request.roomPrefix}, date=${request.date}, since=${request.since}, until=${request.until}, nearMe=${request.nearMe}, limit=${request.limit}, offset=${request.offset}, utilities=${request.utilities}"
                )
                val response = apiService.searchRooms(request)
                Log.d(TAG, "searchRooms response code=${response.code()}, successful=${response.isSuccessful}")
                if (response.isSuccessful) {
                    val body = response.body() ?: RoomSearchResponse()
                    Log.d(TAG, "searchRooms parsed rooms=${body.rooms.size}, total=${body.total}")
                    body.rooms.take(3).forEachIndexed { index, room ->
                        Log.d(
                            TAG,
                            "room[$index] id=${room.id}, name=${room.name}, building=${room.building}, capacity=${room.capacity}"
                        )
                    }
                    Result.success(body)
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    Log.e(TAG, "searchRooms error body=$errorBody")
                    Result.failure(Exception("Error ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "searchRooms exception=${e.message}", e)
                Result.failure(e)
            }
        }

    suspend fun trackHomeEvent(eventName: String) {
        withContext(Dispatchers.IO) {
            try {
                apiService.trackAnalyticsEvent(
                    AnalyticsEventRequest(
                        sessionId = sessionId,
                        eventName = eventName,
                        screen = "home"
                    )
                )
            } catch (_: Exception) {
            }
        }
    }

    suspend fun getRoomAvailability(
        roomId: String,
        dateValue: String
    ): Result<List<RoomTimeWindowDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRoomAvailability(roomId = roomId, dateValue = dateValue)
            if (response.isSuccessful) {
                val windows = response.body()?.toTimeWindows(dateValue).orEmpty()
                Result.success(windows)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIfScheduleExists(): Result<Boolean> {
        return try {
            val response = apiService.getScheduleClasses()
            if (response.isSuccessful) {
                val bodyString = response.body().toString()
                val hasSchedule = !bodyString.contains("\"classes\": []") && bodyString.length > 10
                Result.success(hasSchedule)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("Network error: Check your connection"))
        }
    }

    companion object {
        private const val TAG = "AppRepository"
    }

    class ApiException(message: String) : Exception(message) {
        override val message: String
            get() = super.message?.removePrefix("java.lang.Exception: ")?.trim() ?: "Unknown API Error"
    }
}
