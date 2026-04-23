package com.example.andespace.model.dto

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

data class ScheduleImportStepRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("user_email") val userEmail: String? = null,
    @SerializedName("method") val method: String,
    @SerializedName("step") val step: String,
    @SerializedName("step_number") val stepNumber: Int,
    @SerializedName("timestamp") val timestamp: String
)

data class AnalyticsOkResponse(
    @SerializedName("ok") val ok: Boolean
)