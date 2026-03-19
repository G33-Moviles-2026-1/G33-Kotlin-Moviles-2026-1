package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.andespace.ui.bookings.BookingsUIState
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.detailRoom.DetailRoomUiState
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.results.ResultsUiState
import com.example.andespace.ui.results.ResultsViewModel

@Composable
fun MainClassroomsScreen(
    homepageViewModel: HomepageViewModel,
    resultsViewModel: ResultsViewModel,
    detailRoomViewModel: DetailRoomViewModel,
    bookingsViewModel: BookingsViewModel,
    isUserLoggedIn: Boolean,
    onBookingCreatedNavigate: () -> Unit
) {
    val homepageState by homepageViewModel.uiState.collectAsState()
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    val detailRoomUiState by detailRoomViewModel.uiState.collectAsState()
    val bookingsUiState by bookingsViewModel.uiState.collectAsState()

    LoadClassroomsScreen(
        contentScreen = homepageState.contentScreen,
        isUserLoggedIn = isUserLoggedIn,
        resultsUiState = resultsUiState,
        detailRoomUiState = detailRoomUiState,
        bookingsUiState = bookingsUiState,
        onSearchClick = { params ->
            resultsViewModel.onSearchClick(
                params = params,
                isUserLoggedIn = isUserLoggedIn
            )
            homepageViewModel.onShowResults()
        },
        onFiltersOpened = { homepageViewModel.onFiltersOpened() },
        onRoomClick = { room ->
            resultsViewModel.onRoomClick(room)
            detailRoomViewModel.setRoom(
                room = room,
                selectedDate = resultsUiState.selectedSearchDate
            )
            homepageViewModel.onShowRoomDetailScreen()
        },
        onPrevPage = { resultsViewModel.onPreviousPage(isUserLoggedIn = isUserLoggedIn) },
        onNextPage = { resultsViewModel.onNextPage(isUserLoggedIn = isUserLoggedIn) },
        onRoomDetailDateChange = { dateValue -> detailRoomViewModel.onDateChange(dateValue) },
        onShowMakeBooking = { homepageViewModel.onShowMakeBooking() },
        onCreateBooking = { request -> bookingsViewModel.onCreateBooking(request) },
        onBookingCreatedConsumed = { bookingsViewModel.consumeBookingCreatedSuccess() },
        onBookingCreatedNavigate = {
            onBookingCreatedNavigate()
            homepageViewModel.resetToHome()
        }
    )
}
