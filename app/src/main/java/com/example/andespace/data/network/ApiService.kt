package com.example.andespace.data.network

import com.example.andespace.data.model.dto.AnalyticsEventRequest
import com.example.andespace.data.model.dto.RoomAvailabilityResponse
import com.example.andespace.data.model.dto.RoomSearchRequest
import com.example.andespace.data.model.dto.RoomSearchResponse
import com.example.andespace.data.model.schedule.WeeklyScheduleOut
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val first_semester: String
)

typealias MeResponse = Any

interface ApiService {
    @POST("signup/")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<Any>

    @GET("me/")
    suspend fun checkSession(): Response<MeResponse>

    @POST("logout/")
    suspend fun logout(): Response<Unit>

    @POST("rooms/search")
    suspend fun searchRooms(@Body body: RoomSearchRequest): Response<RoomSearchResponse>

    @POST("analytics/events")
    suspend fun trackAnalyticsEvent(@Body body: AnalyticsEventRequest): Response<Unit>

    @GET("rooms/{roomId}/availability")
    suspend fun getRoomAvailability(
        @Path("roomId") roomId: String,
        @Query("date_value") dateValue: String
    ): Response<RoomAvailabilityResponse>

    @GET("schedule/classes")
    suspend fun getScheduleClasses(): Response<Any>

    @Multipart
    @POST("schedule/upload/ics")
    suspend fun uploadIcsFile(@Part file: MultipartBody.Part): Response<Any>

    @GET("schedule/week")
    suspend fun getWeeklySchedule(
        @Query("date") date: String? = null
    ): Response<WeeklyScheduleOut>
}