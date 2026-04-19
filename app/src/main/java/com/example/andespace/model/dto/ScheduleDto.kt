package com.example.andespace.model.dto

data class WeeklyScheduleOut(
    val week_start: String,
    val week_end: String,
    val occurrences: List<ScheduleClassOccurrenceOut>
)

data class ScheduleClassOccurrenceOut(
    val class_id: String,
    val title: String?,
    val location_text: String?,
    val room_id: String?,
    val date: String,
    val weekday: String,
    val start_time: String,
    val end_time: String
)

data class ManualClassIn(
    val location_text: String? = null,
    val title: String,
    val room_id: String?,
    val start_date: String,
    val end_date: String,
    val start_time: String,
    val end_time: String,
    val weekdays: List<String>
)
data class ManualScheduleIn(
    val classes: List<ManualClassIn>
)

data class ScheduleClassBaseOut(
    val class_id: String,
    val title: String?,
    val location_text: String?,
    val room_id: String?,
    val start_date: String,
    val end_date: String,
    val start_time: String,
    val end_time: String,
    val weekdays: List<String>
)

data class ScheduleClassesOut(
    val classes: List<ScheduleClassBaseOut>
)

data class RoomRecommendationReasonOut(
    val near_previous_class: Boolean,
    val near_next_class: Boolean,
    val frequent_building_match: Boolean,
    val frequent_floor_match: Boolean
)

data class RecommendedRoomOut(
    val room_id: String,
    val building_name: String?,
    val capacity: Int,
    val reliability: Float,
    val score: Float,
    val from_previous_seconds: Float?,
    val to_next_seconds: Float?,
    val matches_frequent_building: Boolean = false,
    val matches_frequent_floor: Boolean = false,
    val reasons: RoomRecommendationReasonOut
)
data class SlotRoomRecommendationsOut(
    val slot_start: String,
    val slot_end: String,
    val recommended_rooms: List<RecommendedRoomOut>
)

data class DayRoomRecommendationsOut(
    val date: String,
    val weekday: String,
    val slots: List<SlotRoomRecommendationsOut>
)