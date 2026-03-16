package com.example.andespace.data.repository

import android.util.Log
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
        return "Uniandes Student"
    }

    suspend fun login(user: String, password: String): Boolean {
        delay(1000)
        return user == "Kotlin" && password == "123"
    }

    fun getHistory() = listOf("ML 001", "W 101", "SD 202")

    suspend fun searchRooms(
        params: HomeSearchParams,
        limit: Int = 20,
        offset: Int = 0
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
                val response = api.searchRooms(request)
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

    companion object {
        private const val TAG = "AppRepository"
    }
}
