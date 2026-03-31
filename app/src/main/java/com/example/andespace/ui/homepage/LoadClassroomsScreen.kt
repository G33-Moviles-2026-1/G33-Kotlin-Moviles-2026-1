package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.bookings.BookingsUIState
import com.example.andespace.ui.bookings.MainMakeBookingScreen
import com.example.andespace.ui.detailRoom.DetailRoomUiState
import com.example.andespace.ui.detailRoom.MainRoomDetailScreen
import com.example.andespace.ui.results.ResultsUiState

@Composable
fun LoadClassroomsScreen(
    contentScreen: ContentScreen,
    isUserLoggedIn: Boolean,
    homepageUiState: HomepageUiState,
    resultsUiState: ResultsUiState,
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
    onBookingCreatedNavigate: () -> Unit
) {
    when (contentScreen) {
        ContentScreen.ROOM_DETAIL -> {
            MainRoomDetailScreen(
                uiState = detailRoomUiState,
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
                isSearching = resultsUiState.isSearching,
                isUserLoggedIn = isUserLoggedIn,
                hasUploadedSchedule = resultsUiState.hasUploadedSchedule,
                closeToMe = homepageUiState.closeToMe,
                isLocating = homepageUiState.isLocating,
                locationError = homepageUiState.locationError,
                userLocation = homepageUiState.userLocation,
                searchError = resultsUiState.errorMessage,
                rooms = resultsUiState.rooms,
                currentPage = resultsUiState.currentPage,
                totalPages = resultsUiState.totalPages,
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
