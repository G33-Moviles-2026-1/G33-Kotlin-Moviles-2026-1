package com.example.andespace.data.api.dto

import com.google.gson.annotations.SerializedName

data class RoomSearchRequest(
    @SerializedName("date") val date: String
)
