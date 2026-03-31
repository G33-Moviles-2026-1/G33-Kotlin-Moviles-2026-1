package com.example.andespace.model.schedule

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