package com.example.andespace.ui.schedule

import com.example.andespace.model.dto.DayRoomRecommendationsOut
import com.example.andespace.model.dto.WeeklyScheduleOut

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val hasSchedule: Boolean = false,
    val scheduleData: WeeklyScheduleOut? = null,
    val errorMessage: String? = null,
    val classTitle: String = "",
    val classRoom: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val selectedDays: Set<String> = emptySet(),
    val isAddingManualClass: Boolean = false,
    val isShowingRecommendations: Boolean = false,
    val recommendationsData: DayRoomRecommendationsOut? = null
)