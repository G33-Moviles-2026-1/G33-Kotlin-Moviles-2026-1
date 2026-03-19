package com.example.andespace.ui.bookings

import androidx.compose.runtime.Composable
import com.example.andespace.data.model.dto.CreateBookingRequest
import com.example.andespace.ui.detailRoom.DetailRoomUiState

@Composable
fun MainMakeBookingScreen(
    detailRoomUiState: DetailRoomUiState,
    bookingsUiState: BookingsUIState,
    onDateChange: (String) -> Unit,
    onCreateBooking: (CreateBookingRequest) -> Unit,
    onBookingCreatedConsumed: () -> Unit,
    onBookingCreatedNavigate: () -> Unit
) {
    LoadMakeBookingScreen(
        detailRoomUiState = detailRoomUiState,
        bookingsUiState = bookingsUiState,
        onDateChange = onDateChange,
        onCreateBooking = onCreateBooking,
        onBookingCreatedConsumed = onBookingCreatedConsumed,
        onBookingCreatedNavigate = onBookingCreatedNavigate
    )
}
