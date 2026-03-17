package com.example.andespace.ui.bookings

import com.example.andespace.data.model.dto.BookingDto

enum class BookingsContentScreen {
    LIST,
    EDIT
}

data class BookingsUIState(
    val bookings: List<BookingDto> = emptyList(),
    val selectedBooking: BookingDto? = null,
    val contentScreen: BookingsContentScreen = BookingsContentScreen.LIST,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCreating: Boolean = false,
    val createError: String? = null,
    val bookingCreatedSuccess: Boolean = false,
    val errorMessage: String? = null,
    val requiresLogin: Boolean = false
)
