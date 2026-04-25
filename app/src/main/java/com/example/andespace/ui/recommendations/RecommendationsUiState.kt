package com.example.andespace.ui.recommendations

import com.example.andespace.model.dto.RoomSearchItemOut
import com.example.andespace.model.dto.TimeWindowOut
import java.time.LocalDate
import java.time.LocalTime

data class RecommendationsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = false,
    val recommendations: List<RoomSearchItemOut> = emptyList(),
    val currentIndex: Int = 0,
    val favoriteIds: Set<String> = emptySet(),
    val error: String? = null,
    val isSearchActive: Boolean = false,
    val showBookingDialog: Boolean = false,
    val bookingPurpose: String = "",
    val isBookingInProgress: Boolean = false,
    val bookingError: String? = null,
    val selectedBookingSlot: TimeWindowOut? = null,
) {
    val currentRoom: RoomSearchItemOut?
        get() = recommendations.getOrNull(currentIndex)
}