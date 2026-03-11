package com.example.andespace.data.api.dto

import com.google.gson.annotations.SerializedName

data class AnalyticsEventRequest(
    @SerializedName("screen") val screen: String = "home",
    @SerializedName("event_type") val eventType: String,
    @SerializedName("payload") val payload: Map<String, String>? = null
)
