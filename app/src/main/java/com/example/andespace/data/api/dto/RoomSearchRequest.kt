package com.example.andespace.data.api.dto

import com.google.gson.annotations.SerializedName

data class RoomSearchRequest(
    @SerializedName("classroom") val classroom: String,
    @SerializedName("date") val date: String,
    @SerializedName("since") val since: String,
    @SerializedName("until") val until: String,
    @SerializedName("close_to_me") val closeToMe: Boolean,
    @SerializedName("utilities") val utilities: List<String>
)
