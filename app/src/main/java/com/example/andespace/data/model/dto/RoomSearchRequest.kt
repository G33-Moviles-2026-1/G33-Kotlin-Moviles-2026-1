package com.example.andespace.data.model.dto

import com.google.gson.annotations.SerializedName

data class UserLocation(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class RoomSearchRequest(
    @SerializedName("room_prefix") val roomPrefix: String? = null,
    @SerializedName("room_prefixes") val roomPrefixes: List<String> = emptyList(),
    @SerializedName("date") val date: String,
    @SerializedName("since") val since: String,
    @SerializedName("until") val until: String,
    @SerializedName("building_codes") val buildingCodes: List<String> = emptyList(),
    @SerializedName("utilities") val utilities: List<String> = emptyList(),
    @SerializedName("near_me") val nearMe: Boolean = false,
    @SerializedName("user_location") val userLocation: UserLocation? = null,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("offset") val offset: Int = 0
)
