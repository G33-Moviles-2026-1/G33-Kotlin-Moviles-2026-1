package com.example.andespace.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.AddFavoriteRequest
import com.example.andespace.model.dto.AnalyticsEventRequest
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.data.repository.AnalyticsEventQueue
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.RoomGapSearchAnalyticsRequest
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.model.dto.RoomSearchRequest
import com.example.andespace.model.dto.UserLocation
import com.example.andespace.model.dto.RoomSearchResponse
import com.example.andespace.model.dto.RoomTimeWindowDto
import com.example.andespace.model.dto.toTimeWindows
import com.example.andespace.model.schedule.WeeklyScheduleOut
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.LoginRequest
import com.example.andespace.data.network.NetworkModule
import com.example.andespace.data.network.RegisterRequest
import com.example.andespace.model.schedule.ManualScheduleIn
import com.example.andespace.model.schedule.ScheduleClassesOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.andespace.model.schedule.DayRoomRecommendationsOut

class AppRepository {
    private val apiService: ApiService = NetworkModule.apiService
    private val sessionId = AnalyticsSessionManager.currentSessionId

    companion object {
        private const val TAG = "AppRepository"
        private const val ANALYTICS_QUEUE_TAG = "AnalyticsQueue"
        const val ANALYTICS_EVENT_FAVORITE_SUBMITTED = "favorite_submitted"
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            apiService.logout()
            NetworkModule.cookieJar.clearCookies()
            Result.success(true)
        } catch (_: Exception) {
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
            } catch (_: Exception) {
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
            val request = AnalyticsEventRequest(
                sessionId = sessionId,
                eventName = eventName,
                screen = "home"
            )
            try {
                apiService.trackAnalyticsEvent(request)
            } catch (_: Exception) {
                AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.Generic(request))
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
            val request = AnalyticsEventRequest(
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
            try {
                apiService.trackAnalyticsEvent(request).isSuccessful
            } catch (_: Exception) {
                AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.Generic(request))
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
            val request = RoomGapSearchAnalyticsRequest(
                sessionId = sessionId,
                dateValue = dateValue,
                gapStart = gapStart.toHhMmSs(),
                gapEnd = gapEnd.toHhMmSs(),
                utilities = utilities
            )
            try {
                apiService.trackRoomGapSearch(request).isSuccessful
            } catch (_: Exception) {
                AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.RoomGapSearch(request))
                false
            }
        }
    }

    suspend fun trackScreensTime(screenName: String) {
        withContext(Dispatchers.IO) {
            val request = AnalyticsEventRequest(
                sessionId = sessionId,
                eventName = "open_screen_timestamp",
                screen = screenName
            )
            try {
                apiService.trackAnalyticsEvent(request)
            } catch (_: Exception) {
                AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.Generic(request))
            }
        }
    }

    suspend fun trackFavoriteEvent(room: RoomDto, added: Boolean) {
        withContext(Dispatchers.IO) {
            val request = AnalyticsEventRequest(
                sessionId = sessionId,
                eventName = ANALYTICS_EVENT_FAVORITE_SUBMITTED,
                screen = "favorites",
                propsJson = mapOf(
                    "room_id" to room.id,
                    "room_name" to (room.name ?: ""),
                    "building" to (room.building ?: ""),
                    "building_code" to (room.buildingCode ?: ""),
                    "action" to if (added) "add" else "remove"
                )
            )
            try {
                val r = apiService.trackAnalyticsEvent(request)
                if (!r.isSuccessful) {
                    AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.Generic(request))
                }
            } catch (_: Exception) {
                AnalyticsEventQueue.enqueue(AnalyticsEventQueue.PendingEvent.Generic(request))
            }
        }
    }

    suspend fun flushPendingAnalytics() {
        val events = AnalyticsEventQueue.drainAll()
        if (events.isEmpty()) {
            Log.d(ANALYTICS_QUEUE_TAG, "FLUSH: cola vacía, nada que enviar")
            return
        }
        Log.d(ANALYTICS_QUEUE_TAG, "FLUSH: intentando enviar ${events.size} evento(s) pendiente(s)")
        val failed = mutableListOf<AnalyticsEventQueue.PendingEvent>()
        for (event in events) {
            try {
                when (event) {
                    is AnalyticsEventQueue.PendingEvent.Generic -> {
                        val r = apiService.trackAnalyticsEvent(event.request)
                        if (!r.isSuccessful) failed.add(event)
                    }
                    is AnalyticsEventQueue.PendingEvent.RoomGapSearch -> {
                        val r = apiService.trackRoomGapSearch(event.request)
                        if (!r.isSuccessful) failed.add(event)
                    }
                }
            } catch (_: Exception) {
                failed.add(event)
            }
        }
        val ok = events.size - failed.size
        if (ok > 0) Log.d(ANALYTICS_QUEUE_TAG, "FLUSH: enviados OK al backend: $ok")
        if (failed.isNotEmpty()) {
            Log.w(ANALYTICS_QUEUE_TAG, "FLUSH: fallaron ${failed.size}, se vuelven a encolar")
            failed.forEach { AnalyticsEventQueue.enqueue(it) }
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

    suspend fun getWeeklySchedule(date: String? = null): WeeklyScheduleOut {
        val response = apiService.getWeeklySchedule(date)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to fetch schedule")
        }
        return response.body() ?: throw Exception("Empty schedule body")
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
        } catch (_: Exception) {
            Result.failure(ApiException("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getMyFavorites(): Result<List<RoomDto>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "getMyFavorites -> fetching from backend")
                val response = apiService.getMyFavorites()
                Log.d(TAG, "getMyFavorites code=${response.code()}, ok=${response.isSuccessful}")
                if (response.isSuccessful) {
                    val items = response.body()?.items ?: emptyList()
                    val rooms = items.mapNotNull { it.toRoomDto() }
                    Log.d(TAG, "getMyFavorites -> parsed ${rooms.size} rooms: ${rooms.map { it.id }}")
                    Result.success(rooms)
                } else {
                    val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                    Log.e(TAG, "getMyFavorites failed: $msg")
                    Result.failure<List<RoomDto>>(ApiException(msg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "getMyFavorites exception: ${e.message}", e)
                Result.failure<List<RoomDto>>(Exception("No internet connection. Please check your network and try again."))
            }
        }
    }

    suspend fun addFavorite(room: RoomDto): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = AddFavoriteRequest(
                roomId = room.id,
                name = room.name,
                building = room.building,
                buildingCode = room.buildingCode,
                capacity = room.capacity,
                utilities = room.utilities
            )
            Log.d(TAG, "addFavorite -> roomId=${room.id}, url=${NetworkModule.apiService}")
            val response = apiService.addFavorite(request)
            Log.d(TAG, "addFavorite response code=${response.code()}, successful=${response.isSuccessful}")
            if (response.isSuccessful || response.code() == 201) {
                Result.success(true)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Log.e(TAG, "addFavorite failed: $msg")
                Result.failure(ApiException(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "addFavorite exception: ${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun deleteFavorite(roomId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "deleteFavorite -> roomId=$roomId")
            val response = apiService.deleteFavorite(roomId)
            Log.d(TAG, "deleteFavorite response code=${response.code()}, successful=${response.isSuccessful}")
            if (response.isSuccessful || response.code() == 204) {
                Result.success(true)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), response.code())
                Log.e(TAG, "deleteFavorite failed: $msg")
                Result.failure(ApiException(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteFavorite exception: ${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
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
        } catch (_: Exception) {
            Result.failure(ApiException("No internet connection. Could not upload the file."))
        }
    }
    suspend fun uploadManualSchedule(payload: ManualScheduleIn) {
        val response = apiService.uploadManualSchedule(payload)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to upload schedule")
        }
    }

    suspend fun getScheduleClasses(): ScheduleClassesOut {
        val response = apiService.getScheduleClasses()
        if (response.isSuccessful) {
            return response.body() ?: ScheduleClassesOut(emptyList())
        }
        return ScheduleClassesOut(emptyList())
    }

    suspend fun deleteSchedule() {
        val response = apiService.deleteSchedule()
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to delete schedule")
        }
    }

    suspend fun deleteClass(classId: String) {
        val response = apiService.deleteClass(classId)
        if (!response.isSuccessful) {
            val errorDetails = response.errorBody()?.string()
            println("FASTAPI DELETE CLASS ERROR: $errorDetails")
            throw Exception("Error ${response.code()}: Failed to delete class")
        }
    }

    suspend fun getRoomRecommendationsForDay(date: String): DayRoomRecommendationsOut {
        val response = apiService.getRoomRecommendationsForDay(date)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: Failed to fetch recommendations")
        }
        return response.body() ?: throw Exception("Empty recommendations body")
    }
}

class ApiException(message: String) : Exception(message) {
    override val message: String
        get() = super.message?.removePrefix("java.lang.Exception: ")?.trim()
            ?: "Something went wrong. Please try again."
}
