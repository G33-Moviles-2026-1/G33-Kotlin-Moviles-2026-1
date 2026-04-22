package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName

data class NavigationPathResponse(
    @SerializedName("from_room") val fromClassroom:String,
    @SerializedName("to_room") val toClassroom: String,
    @SerializedName("total_time_seconds") val totalTime: Int,
    @SerializedName("steps") val steps: List<String>
)

data class NavigationPathSearchParams(
    @SerializedName("from_room") val fromClassroom:String? = null,
    @SerializedName("to_room") val toClassroom: String? = null,
)