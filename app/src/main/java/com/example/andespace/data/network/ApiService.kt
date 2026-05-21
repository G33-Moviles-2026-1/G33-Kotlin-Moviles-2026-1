package com.example.andespace.data.network

import com.example.andespace.model.dto.AcceptFriendshipRequest
import com.example.andespace.model.dto.AddFavoriteRequest
import com.example.andespace.model.dto.AnalyticsEventRequest
import com.example.andespace.model.dto.AutoSearchRequest
import com.example.andespace.model.dto.BookingDto
import com.example.andespace.model.dto.ChangeEmailRequest
import com.example.andespace.model.dto.ChangePasswordRequest
import com.example.andespace.model.dto.ChangeStatusRequest
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.CreateFriendshipRequest
import com.example.andespace.model.dto.GetFavoritesResponse
import com.example.andespace.model.dto.MyBookingsResponse
import com.example.andespace.model.dto.NotificationsResponse
import com.example.andespace.model.dto.RoomGapSearchAnalyticsRequest
import com.example.andespace.model.dto.RoomSearchRequest
import com.example.andespace.model.dto.RoomSearchResponse
import com.example.andespace.model.dto.DayRoomRecommendationsOut
import com.example.andespace.model.dto.InteractionPayload
import com.example.andespace.model.dto.ManualScheduleIn
import com.example.andespace.model.dto.MyFriendsResponse
import com.example.andespace.model.dto.NavigationNearestNodeResponse
import com.example.andespace.model.dto.NavigationPathResponse
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.model.dto.RoomSearchItemOut
import com.example.andespace.model.dto.ScheduleClassesOut
import com.example.andespace.model.dto.UserShareScheduleOut
import com.example.andespace.model.dto.UserShareScheduleUpdate
import com.example.andespace.model.dto.WeeklyScheduleOut
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @PUT("me/share-schedule")
    suspend fun updateShareSchedule(@Body payload: UserShareScheduleUpdate): Response<Any>

    @GET("me/share-schedule")
    suspend fun getShareScheduleState(): Response<UserShareScheduleOut>

    @GET("friendships/mine")
    suspend fun getMyFriends(): Response<MyFriendsResponse>

    @GET("schedule/{target_email}/week")
    suspend fun getFriendWeeklySchedule(
        @Path("target_email") targetEmail: String,
        @Query("date") date: String? = null
    ): Response<WeeklyScheduleOut>

    @POST("friendships/")
    suspend fun sendFriendRequest(@Body payload: CreateFriendshipRequest): Response<Any>

    @GET("friendships/requests/incoming")
    suspend fun getIncomingRequests(): Response<MyFriendsResponse>

    @PUT("friendships/accept")
    suspend fun acceptFriendRequest(@Body payload: AcceptFriendshipRequest): Response<Any>

    @GET("friendships/suggestions")
    suspend fun getFriendSuggestions(): Response<List<String>>

    @DELETE("friendships/{friend_email}")
    suspend fun deleteFriendship(@Path("friend_email") friendEmail: String): Response<Unit>

    @GET("me/")
    suspend fun checkSession(): Response<MeResponse>

    @POST("logout/")
    suspend fun logout(): Response<Unit>

    @POST("rooms/search")
    suspend fun searchRooms(@Body body: RoomSearchRequest): Response<RoomSearchResponse>

    @POST("analytics/events")
    suspend fun trackAnalyticsEvent(@Body body: AnalyticsEventRequest): Response<Unit>

    @POST("analytics/room-gap-search")
    suspend fun trackRoomGapSearch(@Body body: RoomGapSearchAnalyticsRequest): Response<Unit>

    @GET("rooms/{roomId}/availability")
    suspend fun getRoomAvailability(
        @Path("roomId") roomId: String,
        @Query("date_value") dateValue: String
    ): Response<RoomDto>

    @GET("bookings/mine")
    suspend fun getMyBookings(): Response<MyBookingsResponse>

    @POST("bookings/")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<BookingDto>

    @DELETE("bookings/{bookingId}")
    suspend fun deleteBooking(@Path("bookingId") bookingId: String): Response<Unit>
    @GET("schedule/classes")
    suspend fun getScheduleClasses(): Response<ScheduleClassesOut>

    @POST("recommendations/auto-search")
    suspend fun getAutoSearchRecommendations(
        @Body request: AutoSearchRequest
    ): List<RoomSearchItemOut>

    @POST("recommendations/interact")
    suspend fun submitRoomInteraction(
        @Body payload: InteractionPayload
    )

    @Multipart
    @POST("schedule/upload/ics")
    suspend fun uploadIcsFile(@Part file: MultipartBody.Part): Response<Any>

    @POST("schedule/upload/manual")
    suspend fun uploadManualSchedule(
        @Body payload: ManualScheduleIn
    ): Response<Any>

    @DELETE("schedule/")
    suspend fun deleteSchedule(): Response<Any>

    @DELETE("schedule/class/{class_id}")
    suspend fun deleteClass(
        @Path("class_id") classId: String
    ): Response<Any>

    @GET("schedule/recommendations/day")
    suspend fun getRoomRecommendationsForDay(
        @Query("date") date: String
    ): Response<DayRoomRecommendationsOut>

    @POST("favorites/")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<Unit>

    @GET("favorites/mine")
    suspend fun getMyFavorites(): Response<GetFavoritesResponse>

    @DELETE("favorites/{roomId}")
    suspend fun deleteFavorite(@Path("roomId") roomId: String): Response<Unit>

    @GET("navigation/path")
    suspend fun getNavigationPath(
        @Query("from_room") fromRoom: String?,
        @Query("to_room") toRoom: String?
    ): Response<NavigationPathResponse>

    @GET("navigation/nearest-node")
    suspend fun getNearestNavigationNode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): Response<NavigationNearestNodeResponse>

    @GET("notifications/")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationRead(@Path("notificationId") id: String): Response<Unit>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Unit>

    @PUT("me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @PUT("me/email")
    suspend fun changeEmail(@Body request: ChangeEmailRequest): Response<Unit>

    @PUT("me/status")
    suspend fun changeStatus(@Body request: ChangeStatusRequest): Response<Unit>

}

