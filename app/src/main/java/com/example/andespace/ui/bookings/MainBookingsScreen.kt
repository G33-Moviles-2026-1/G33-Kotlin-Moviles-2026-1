package com.example.andespace.ui.bookings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MainBookingsScreen(
    bookingsViewModel: BookingsViewModel,
    onRequireLogin: () -> Unit
) {
    val uiState by bookingsViewModel.uiState.collectAsState()

    LoadBookingsScreen(
        uiState = uiState,
        onLoadBookings = { bookingsViewModel.loadBookings() },
        onRequireLogin = onRequireLogin,
        onDeleteBooking = { booking -> bookingsViewModel.onDeleteBooking(booking) },
        onEditBooking = { booking -> bookingsViewModel.onEditBooking(booking) },
        onSaveBooking = { request, oldId -> bookingsViewModel.onSaveBooking(request, oldId) },
        onCancelEdit = { bookingsViewModel.onCancelEdit() },
        onConsumeSyncMessage = { bookingsViewModel.consumeSyncMessage() }
    )
}
