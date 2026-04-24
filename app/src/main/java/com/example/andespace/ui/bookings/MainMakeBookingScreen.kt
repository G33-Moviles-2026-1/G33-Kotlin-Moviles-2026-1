package com.example.andespace.ui.bookings

import androidx.compose.runtime.Composable
import com.example.andespace.ui.detailRoom.DetailRoomViewModel

@Composable
fun MainMakeBookingScreen(
    onBookingCreatedNavigate: () -> Unit,
    detailRoomViewModel: DetailRoomViewModel,
    bookingsViewModel: BookingsViewModel
) {

    LoadMakeBookingScreen(
        detailRoomViewModel = detailRoomViewModel,
        bookingsViewModel = bookingsViewModel,
        onDateChange = { dateValue -> detailRoomViewModel.onDateChange(dateValue) },
        onCreateBooking = { request -> bookingsViewModel.onCreateBooking(request) },
        onBookingCreatedConsumed = { bookingsViewModel.consumeBookingCreatedSuccess() },
        onBookingCreatedNavigate = onBookingCreatedNavigate
    )
}
