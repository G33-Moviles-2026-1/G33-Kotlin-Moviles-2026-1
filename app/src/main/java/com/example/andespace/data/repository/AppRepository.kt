package com.example.andespace.data.repository

import com.example.andespace.data.api.RetrofitClient
import com.example.andespace.data.api.dto.AnalyticsEventRequest
import com.example.andespace.data.api.dto.RoomSearchRequest
import com.example.andespace.data.api.dto.RoomSearchResponse
import com.example.andespace.data.model.HomeSearchParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository {

    private val api = RetrofitClient.apiService
    private val sessionId = UUID.randomUUID().toString()

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
                    roomPrefix = params.classroom.ifBlank { null },
                    date = params.date,
                    since = "${params.since}:00",
                    until = "${params.until}:00",
                    utilities = params.utilities,
                    nearMe = params.closeToMe
                )
                val response = api.searchRooms(request)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: RoomSearchResponse())
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    Result.failure(Exception("Error ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun trackHomeEvent(eventName: String) = withContext(Dispatchers.IO) {
        try {
            api.trackAnalyticsEvent(
                AnalyticsEventRequest(
                    sessionId = sessionId,
                    eventName = eventName,
                    screen = "home"
                )
            )
        } catch (_: Exception) { }
    }
}
