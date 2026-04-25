package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.data.repository.shared.httpErrorMessage
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.model.dto.RoomSearchRequest
import com.example.andespace.model.dto.RoomSearchResponse
import com.example.andespace.model.dto.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RoomRepository(private val apiService: ApiService) {
    companion object {
        private const val TAG = "RoomsRepository"
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
                    since = "${params.since ?: "00:00"}:00",
                    until = "${params.until ?: "23:59"}:00",
                    utilities = params.utilities,
                    nearMe = params.closeToMe,
                    userLocation = if (params.closeToMe && params.userLatitude != null && params.userLongitude != null) {
                        UserLocation(params.userLatitude, params.userLongitude)
                    } else null,
                    limit = limit,
                    offset = offset
                )
                Log.d(
                    TAG,
                    "searchRooms request -> roomPrefix=${request.roomPrefix}, date=${request.date}, since=${request.since}, until=${request.until}, nearMe=${request.nearMe}, limit=${request.limit}, offset=${request.offset}, utilities=${request.utilities}"
                )
                val response = apiService.searchRooms(request)
                Log.d(
                    TAG,
                    "searchRooms response code=${response.code()}, successful=${response.isSuccessful}"
                )
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
                    val rawErrorBody = response.errorBody()?.string()
                    Log.e(
                        TAG,
                        "searchRooms error code=${response.code()}, backend_message=$rawErrorBody"
                    )
                    Result.failure(Exception(extractErrorMessage(rawErrorBody, response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "searchRooms exception=${e.message}", e)
                Result.failure(Exception("No internet connection. Please check your network and try again."))
            }
        }

    suspend fun getRoomAvailability(
        roomId: String,
        dateValue: String
    ): Result<RoomDto> = try {
        val response = apiService.getRoomAvailability(roomId = roomId, dateValue = dateValue)
        if (response.isSuccessful) {
            val room = response.body()
            if (room != null) {
                Result.success(room)
            } else {
                Result.failure(Exception("Could not load the room's availability. Please try again."))
            }
        } else {
            Result.failure(Exception(httpErrorMessage(response.code())))
        }
    } catch (_: Exception) {
        Result.failure(Exception("No internet connection. Please check your network and try again."))
    }
}