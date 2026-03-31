package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName

data class UserFreeSlotsResponse(
    @SerializedName("date") val date: String? = null,
    @SerializedName("weekday") val weekday: String? = null,
    @SerializedName("free_slots") val freeSlots: List<UserFreeSlotDto> = emptyList()
)

data class UserFreeSlotDto(
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null
)
