package com.example.andespace.data.model.dto

import com.google.gson.annotations.SerializedName

data class BookingDto(
    @SerializedName("id") val id: String,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("date") val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("purpose") val purpose: String,
    @SerializedName("status") val status: String = "active",
    @SerializedName("created_at") val createdAt: String? = null
)

data class MyBookingsResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<BookingDto>
)

data class CreateBookingRequest(
    @SerializedName("room_id") val roomId: String,
    @SerializedName("date") val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("purpose") val purpose: String
)
