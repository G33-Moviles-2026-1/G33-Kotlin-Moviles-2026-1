package com.example.andespace.ui.bookings

import com.example.andespace.model.dto.BookingDto

enum class BookingsContentScreen {
    LIST,
    EDIT
}

data class BookingsUIState(
    val bookings: List<BookingDto> = emptyList(),
    val selectedBooking: BookingDto? = null,
    val contentScreen: BookingsContentScreen = BookingsContentScreen.LIST,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val isCreating: Boolean = false,
    val bookingCreatedSuccess: Boolean = false,
    val requiresLogin: Boolean = false,
    val isShowingCached: Boolean = false
)
