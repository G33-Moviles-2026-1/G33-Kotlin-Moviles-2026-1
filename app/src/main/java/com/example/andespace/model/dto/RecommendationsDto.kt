package com.example.andespace.model.dto

data class InteractionPayload(
    val room_id: String,
    val action: InteractionAction,
    val weekday: String,
    val slot_start: String
)
data class TimeWindowOut(val start: String, val end: String)

data class RoomSearchItemOut(
    val room_id: String,
    val room_number: String?,
    val building_name: String,
    val building_code: String,
    val capacity: Int,
    val matching_windows: List<TimeWindowOut> = emptyList(),
    val utilities: List<String> = emptyList(),
)

data class AutoSearchRequest(
    val target_date: String,
    val target_time: String,
    val top_k: Int,
    val exclude_ids: List<String>
)

enum class InteractionAction { SKIP, FAVORITE, BOOK }