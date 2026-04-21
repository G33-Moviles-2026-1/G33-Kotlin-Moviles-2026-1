package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RoomSearchResponse(
    @SerializedName(value = "items", alternate = ["rooms"]) val rooms: List<RoomDto> = emptyList(),
    @SerializedName("total") val total: Int? = null
)

data class RoomDto(
    @SerializedName(value = "room_id", alternate = ["id"]) val id: String,
    @SerializedName(value = "room_number", alternate = ["name"]) val name: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("weekday") val weekday: String? = null,
    @SerializedName(value = "building_name", alternate = ["building"]) val building: String? = null,
    @SerializedName("building_code") val buildingCode: String? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("reliability") val reliability: Double? = null,
    @SerializedName("distance_seconds") val distanceSeconds: Double? = null,
    @SerializedName("wait_seconds") val waitSeconds: Int? = null,
    @SerializedName("matching_windows") val matchingWindows: List<RoomTimeWindowDto>? = emptyList(),
    @SerializedName("utilities") val utilities: List<String>? = emptyList(),
    @SerializedName("available_slots") val availableSlots: List<RoomWeeklyAvailabilityDto>? = emptyList(),
    @SerializedName("blocked_slots") val blockedSlots: List<AvailabilitySlotDto>? = emptyList()
)

data class AvailabilitySlotDto(
    @SerializedName(
        value = "start",
        alternate = ["since", "from", "start_time"]
    ) val start: String? = null,
    @SerializedName(value = "end", alternate = ["until", "to", "end_time"]) val end: String? = null,
    @SerializedName("is_available") val isAvailable: Boolean? = null
)

data class RoomTimeWindowDto(
    @SerializedName(
        value = "start",
        alternate = ["since", "from", "start_time"]
    ) val start: String? = null,
    @SerializedName(value = "end", alternate = ["until", "to", "end_time"]) val end: String? = null
)


data class RoomWeeklyAvailabilityDto(
    @SerializedName(
        value = "start",
        alternate = ["since", "from", "start_time"]
    ) val start: String? = null,
    @SerializedName(value = "end", alternate = ["until", "to", "end_time"]) val end: String? = null,
    @SerializedName("is_available") val isAvailable: Boolean? = null
)
