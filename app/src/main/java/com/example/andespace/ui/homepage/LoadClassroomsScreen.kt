package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.bookings.BookingsUIState
import com.example.andespace.ui.bookings.MainMakeBookingScreen
import com.example.andespace.ui.detailRoom.DetailRoomUiState
import com.example.andespace.ui.detailRoom.DetailRoomViewModel
import com.example.andespace.ui.detailRoom.RoomDetailScreen
import com.example.andespace.ui.results.ResultsUiState
import com.example.andespace.ui.results.ResultsViewModel

@Composable
fun LoadClassroomsScreen(
    contentScreen: ContentScreen,
    isUserLoggedIn: Boolean,
    homepageUiState: HomepageUiState,
    detailRoomUiState: DetailRoomUiState,
    bookingsUiState: BookingsUIState,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onRoomDetailDateChange: (String) -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
    onShowMakeBooking: () -> Unit,
    onCreateBooking: (CreateBookingRequest) -> Unit,
    onBookingCreatedConsumed: () -> Unit,
    onBookingCreatedNavigate: () -> Unit,
    resultsViewModel: ResultsViewModel,
    detailRoomViewModel: DetailRoomViewModel
) {
    when (contentScreen) {
        ContentScreen.ROOM_DETAIL -> {
            RoomDetailScreen(
                detailRoomViewModel = detailRoomViewModel,
                onDateChange = onRoomDetailDateChange,
                onBookRoom = onShowMakeBooking
            )
        }

        ContentScreen.MAKE_BOOKING -> {
            MainMakeBookingScreen(
                detailRoomUiState = detailRoomUiState,
                bookingsUiState = bookingsUiState,
                onDateChange = onRoomDetailDateChange,
                onCreateBooking = onCreateBooking,
                onBookingCreatedConsumed = onBookingCreatedConsumed,
                onBookingCreatedNavigate = onBookingCreatedNavigate
            )
        }

        else -> {
            MainHomePageScreen(
                contentScreen = contentScreen,
                isUserLoggedIn = isUserLoggedIn,
                closeToMe = homepageUiState.closeToMe,
                isLocating = homepageUiState.isLocating,
                locationError = homepageUiState.locationError,
                userLocation = homepageUiState.userLocation,
                resultsViewModel = resultsViewModel,
                onSearchClick = onSearchClick,
                onFiltersOpened = onFiltersOpened,
                onRequestCurrentLocation = onRequestCurrentLocation,
                onLocationPermissionDenied = onLocationPermissionDenied,
                onCloseToMeDisabled = onCloseToMeDisabled,
                onClearLocationError = onClearLocationError,
                onRoomClick = onRoomClick,
                onPrevPage = onPrevPage,
                onNextPage = onNextPage
            )
        }
    }
}
