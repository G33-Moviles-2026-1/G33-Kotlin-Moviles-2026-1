package com.example.andespace.ui.homepage

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.andespace.data.location.FusedLocationSensor
import com.example.andespace.ui.bookings.BookingsViewModel
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.results.ResultsViewModel

@Composable
fun MainClassroomsScreen(
    homepageViewModel: HomepageViewModel,
    resultsViewModel: ResultsViewModel,
    detailRoomViewModel: DetailRoomViewModel,
    bookingsViewModel: BookingsViewModel,
    isUserLoggedIn: Boolean,
    onRequireLogin: () -> Unit,
    onBookingCreatedNavigate: () -> Unit
) {
    val homepageState by homepageViewModel.uiState.collectAsState()
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    val detailRoomUiState by detailRoomViewModel.uiState.collectAsState()
    val bookingsUiState by bookingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationSensor = remember(context) { FusedLocationSensor(context.applicationContext) }

    BackHandler(enabled = homepageState.contentScreen != ContentScreen.HOME) {
        homepageViewModel.onBackPressedInSearchFlow()
    }

    LoadClassroomsScreen(
        contentScreen = homepageState.contentScreen,
        isUserLoggedIn = isUserLoggedIn,
        homepageUiState = homepageState,
        resultsUiState = resultsUiState,
        detailRoomUiState = detailRoomUiState,
        bookingsUiState = bookingsUiState,
        onSearchClick = { params ->
            resultsViewModel.onSearchClick(
                params = params
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
        onPrevPage = { resultsViewModel.onPreviousPage() },
        onNextPage = { resultsViewModel.onNextPage() },
        onRoomDetailDateChange = { dateValue -> detailRoomViewModel.onDateChange(dateValue) },
        onRequestCurrentLocation = { homepageViewModel.requestCurrentLocation(locationSensor) },
        onLocationPermissionDenied = { homepageViewModel.onLocationPermissionDenied() },
        onCloseToMeDisabled = { homepageViewModel.onCloseToMeDisabled() },
        onClearLocationError = { homepageViewModel.clearLocationError() },
        onShowMakeBooking = { if (isUserLoggedIn) homepageViewModel.onShowMakeBooking() else onRequireLogin() },
        onCreateBooking = { request -> bookingsViewModel.onCreateBooking(request) },
        onBookingCreatedConsumed = { bookingsViewModel.consumeBookingCreatedSuccess() },
        onBookingCreatedNavigate = {
            onBookingCreatedNavigate()
            homepageViewModel.resetToHome()
        }
    )
}
