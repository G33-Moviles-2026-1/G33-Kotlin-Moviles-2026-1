package com.example.andespace.ui.schedule

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val hasSchedule: Boolean = false,
    val errorMessage: String? = null
)