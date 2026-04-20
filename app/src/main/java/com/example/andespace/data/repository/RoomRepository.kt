package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.ScheduleValidator
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.data.repository.shared.httpErrorMessage
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomSearchRequest
import com.example.andespace.model.dto.RoomSearchResponse
import com.example.andespace.model.dto.RoomTimeWindowDto
import com.example.andespace.model.dto.UserLocation
import com.example.andespace.model.dto.toTimeWindows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale


class RoomRepository(private val apiService: ApiService) {
    private val scheduleValidator: ScheduleValidator = ScheduleValidator(apiService)
    companion object {
        private const val TAG = "RoomsRepository"
    }

    suspend fun checkIfScheduleExists(): Result<Boolean> {
        return scheduleValidator.checkIfScheduleExists()
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
                    Log.e(TAG, "searchRooms error code=${response.code()}, backend_message=$rawErrorBody")
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
    ): Result<List<RoomTimeWindowDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRoomAvailability(roomId = roomId, dateValue = dateValue)
            if (response.isSuccessful) {
                val windows = response.body()?.toTimeWindows(dateValue).orEmpty()
                Result.success(windows)
            } else {
                Result.failure(Exception(httpErrorMessage(response.code())))
            }
        } catch (_: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getUserFreeSlots(dateValue: String): Result<List<RoomTimeWindowDto>> =
        withContext(Dispatchers.IO) {
            try {
                val formattedDate = dateValue.toScheduleDateParam()
                val response = apiService.getUserFreeSlots(date = formattedDate)
                if (response.isSuccessful) {
                    val slots = response.body()?.freeSlots.orEmpty()
                        .mapNotNull { slot ->
                            val start = slot.startTime
                            val end = slot.endTime
                            if (start.isNullOrBlank() || end.isNullOrBlank()) {
                                null
                            } else {
                                RoomTimeWindowDto(start = start, end = end)
                            }
                        }
                    Result.success(slots)
                } else {
                    Result.failure(Exception(httpErrorMessage(response.code())))
                }
            } catch (_: Exception) {
                Result.failure(Exception("No internet connection. Please check your network and try again."))
            }
        }

    private fun String.toScheduleDateParam(): String {
        return runCatching {
            val source = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val target = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val parsedDate = source.parse(this)
            if (parsedDate != null) target.format(parsedDate) else this
        }.getOrDefault(this)

    }
}