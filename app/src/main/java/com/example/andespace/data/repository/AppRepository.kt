package com.example.andespace.data.repository

import com.example.andespace.data.api.RetrofitClient
import com.example.andespace.data.api.dto.AnalyticsEventRequest
import com.example.andespace.data.api.dto.RoomSearchRequest
import com.example.andespace.data.api.dto.RoomSearchResponse
import com.example.andespace.data.model.HomeSearchParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository {

    private val api = RetrofitClient.apiService

    suspend fun getUserName(): String {
        delay(1000)
        return "Estudiante Uniandes"
    }

    suspend fun login(user: String, password: String): Boolean {
        delay(1000)
        return user == "Kotlin" && password == "123"
    }

    fun getHistory() = listOf("ML 001", "W 101", "SD 202")

    suspend fun searchRooms(params: HomeSearchParams): Result<RoomSearchResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = RoomSearchRequest(
                    classroom = params.classroom,
                    date = params.date,
                    since = params.since,
                    until = params.until,
                    closeToMe = params.closeToMe,
                    utilities = params.utilities
                )
                val response = api.searchRooms(request)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: RoomSearchResponse())
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun trackHomeEvent(eventType: String) = withContext(Dispatchers.IO) {
        try {
            api.trackAnalyticsEvent(AnalyticsEventRequest(screen = "home", eventType = eventType))
        } catch (_: Exception) { }
    }
}
