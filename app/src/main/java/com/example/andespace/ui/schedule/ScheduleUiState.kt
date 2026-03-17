package com.example.andespace.ui.schedule

import com.example.andespace.data.model.schedule.WeeklyScheduleOut

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val hasSchedule: Boolean = false,
    val scheduleData: WeeklyScheduleOut? = null,
    val errorMessage: String? = null
)