package com.example.andespace.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.andespace.data.model.HomeSearchParams
import com.example.andespace.data.model.dto.AnalyticsEventRequest
import com.example.andespace.data.model.dto.BookingDto
import com.example.andespace.data.model.dto.CreateBookingRequest
import com.example.andespace.data.model.dto.RoomGapSearchAnalyticsRequest
import com.example.andespace.data.model.dto.RoomSearchRequest
import com.example.andespace.data.model.dto.UserLocation
import com.example.andespace.data.model.dto.RoomSearchResponse
import com.example.andespace.data.model.dto.RoomTimeWindowDto
import com.example.andespace.data.model.dto.toTimeWindows
import com.example.andespace.data.model.schedule.WeeklyScheduleOut
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.LoginRequest
import com.example.andespace.data.network.NetworkModule
import com.example.andespace.data.network.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class AppRepository {
    private val apiService: ApiService = NetworkModule.apiService
    private val sessionId = AnalyticsSessionManager.currentSessionId

    companion object {
        private const val TAG = "AppRepository"
    }



    suspend fun register(email: String, password: String, semester: String): Result<Boolean> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, semester))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getMeData(): Result<String> {
        return try {
            val response = apiService.checkSession()
            if (response.isSuccessful) {
                val dataString = response.body().toString()
                Result.success(dataString)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            apiService.logout()
            NetworkModule.cookieJar.clearCookies()
            Result.success(true)
        } catch (e: Exception) {
            NetworkModule.cookieJar.clearCookies()
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    private fun extractErrorMessage(errorBody: String?, defaultCode: Int): String {
        if (!errorBody.isNullOrEmpty()) {
            return try {
                val jsonObject = JSONObject(errorBody)
                when {
                    jsonObject.has("detail") -> jsonObject.getString("detail")
                    jsonObject.has("message") -> jsonObject.getString("message")
                    else -> httpErrorMessage(defaultCode)
                }
            } catch (e: Exception) {
                httpErrorMessage(defaultCode)
            }
        }
        return httpErrorMessage(defaultCode)
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        400 -> "The request was not valid. Please check your input."
        401 -> "Your session has expired. Please log in again."
        403 -> "You don't have permission to perform this action."
        404 -> "The requested information was not found."
        408, 504 -> "The request took too long. Please try again."
        409 -> "There was a conflict with your request. Please try again."
        422 -> "The submitted information is not valid."
        429 -> "Too many requests. Please wait a moment and try again."
        500, 502, 503 -> "The server is temporarily unavailable. Please try again later."
        else -> "Something went wrong. Please try again."
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
                    Log.e(TAG, "searchRooms error code=${response.code()}")
                    Result.failure(Exception(httpErrorMessage(response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "searchRooms exception=${e.message}", e)
                Result.failure(Exception("No internet connection. Please check your network and try again."))
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

    suspend fun trackAppliedFilters(
        placeUsed: Boolean,
        timeUsed: Boolean,
        utilitiesUsed: Boolean,
        closeToMeUsed: Boolean
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.trackAnalyticsEvent(
                    AnalyticsEventRequest(
                        sessionId = sessionId,
                        eventName = "home_filters_opened",
                        screen = "home",
                        propsJson = mapOf(
                            "place" to placeUsed,
                            "time" to timeUsed,
                            "utilities" to utilitiesUsed,
                            "close_to_me" to closeToMeUsed
                        )
                    )
                )
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun trackRoomGapSearch(
        dateValue: String,
        gapStart: String,
        gapEnd: String,
        utilities: List<String>
    ): Boolean {
        if (utilities.isEmpty()) return false

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.trackRoomGapSearch(
                    RoomGapSearchAnalyticsRequest(
                        sessionId = sessionId,
                        dateValue = dateValue,
                        gapStart = gapStart.toHhMmSs(),
                        gapEnd = gapEnd.toHhMmSs(),
                        utilities = utilities
                    )
                )
                response.isSuccessful
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun trackScreensTime(
        screenName: String,
    ) {
        try {
            val payload = AnalyticsEventRequest(
                sessionId = sessionId,
                eventName = "open_screen_timestamp",
                screen = screenName
            )
            apiService.trackAnalyticsEvent(payload)
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
            } catch (e: Exception) {
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

    private fun String.toHhMmSs(): String {
        val trimmed = trim()
        return if (trimmed.count { it == ':' } == 1) "$trimmed:00" else trimmed
    }

    suspend fun getMyBookings(): Result<List<BookingDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyBookings()
            if (response.isSuccessful) {
                Result.success(response.body()?.items.orEmpty())
            } else {
                val code = response.code()
                Result.failure(ApiException(if (code == 401) "SESSION_EXPIRED" else httpErrorMessage(code)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMyBookings exception=${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBooking(request)
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Exception("The booking could not be confirmed. Please try again."))
                } else {
                    Result.failure(ApiException(httpErrorMessage(response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBooking exception=${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getWeeklySchedule(): Result<WeeklyScheduleOut> {
        return try {
            val response = apiService.getWeeklySchedule()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("No internet connection. Could not load the schedule."))
        }
    }

    suspend fun deleteBooking(bookingId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteBooking(bookingId)
                if (response.isSuccessful || response.code() == 204) {
                    Result.success(true)
                } else {
                    Result.failure(ApiException(httpErrorMessage(response.code())))
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteBooking exception=${e.message}", e)
                Result.failure(Exception("No internet connection. Please check your network and try again."))

            }
        }

    suspend fun checkIfScheduleExists(): Result<Boolean> {
        return try {
            val response = apiService.getScheduleClasses()
            if (response.isSuccessful) {
                val bodyString = response.body().toString()
                val hasSchedule =
                    !bodyString.contains("\"classes\": []") && bodyString.length > 10
                Result.success(hasSchedule)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun uploadIcs(context: Context, fileUri: Uri): Result<Boolean> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)
                ?: return Result.failure(Exception("Could not open the selected file"))

            val fileBytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = fileBytes.toRequestBody("text/calendar".toMediaTypeOrNull())

            val multipartPart =
                MultipartBody.Part.createFormData("file", "schedule.ics", requestBody)

            val response = apiService.uploadIcsFile(multipartPart)

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("No internet connection. Could not upload the file."))
        }
    }
}

class ApiException(message: String) : Exception(message) {
    override val message: String
        get() = super.message?.removePrefix("java.lang.Exception: ")?.trim()
            ?: "Something went wrong. Please try again."
}
