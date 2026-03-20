package com.example.andespace.data.model.dto

import com.google.gson.annotations.SerializedName

data class AnalyticsEventRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("user_email") val userEmail: String? = null,
    @SerializedName("event_name") val eventName: String,
    @SerializedName("screen") val screen: String = "home",
    @SerializedName("duration_ms") val durationMs: Int? = null,
    @SerializedName("props_json") val propsJson: Map<String, Any?>? = null
)

data class RoomGapSearchAnalyticsRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("user_email") val userEmail: String? = null,
    @SerializedName("date_value") val dateValue: String,
    @SerializedName("gap_start") val gapStart: String,
    @SerializedName("gap_end") val gapEnd: String,
    @SerializedName("utilities") val utilities: List<String>,
    @SerializedName("props_json") val propsJson: Map<String, Any?> = emptyMap()
)