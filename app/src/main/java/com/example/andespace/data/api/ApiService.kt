package com.example.andespace.data.api

import com.example.andespace.data.api.dto.AnalyticsEventRequest
import com.example.andespace.data.api.dto.RoomSearchRequest
import com.example.andespace.data.api.dto.RoomSearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("rooms/search")
    suspend fun searchRooms(@Body body: RoomSearchRequest): Response<RoomSearchResponse>

    @POST("analytics/events")
    suspend fun trackAnalyticsEvent(@Body body: AnalyticsEventRequest): Response<Unit>
}
