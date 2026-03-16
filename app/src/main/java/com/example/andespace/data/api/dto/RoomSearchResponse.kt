package com.example.andespace.data.api.dto

import com.google.gson.annotations.SerializedName

data class RoomSearchResponse(
    @SerializedName(value = "items", alternate = ["rooms"]) val rooms: List<RoomDto> = emptyList(),
    @SerializedName("total") val total: Int? = null
)

data class RoomDto(
    @SerializedName(value = "room_id", alternate = ["id"]) val id: String,
    @SerializedName(value = "room_number", alternate = ["name"]) val name: String? = null,
    @SerializedName(value = "building_name", alternate = ["building", "building_code"]) val building: String? = null,
    @SerializedName("capacity") val capacity: Int? = null,
    @SerializedName("availability_status") val availabilityStatus: String? = null,
    @SerializedName("available_since") val availableSince: String? = null,
    @SerializedName("available_until") val availableUntil: String? = null,
    @SerializedName("wait_seconds") val waitSeconds: Int? = null,
    @SerializedName("utilities") val utilities: List<String> = emptyList()
)
