package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.andespace.data.location.GeoLocation
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.ui.results.ResultsViewModel

@Composable
fun MainHomePageScreen(
    modifier: Modifier = Modifier,
    contentScreen: ContentScreen,
    isUserLoggedIn: Boolean,
    closeToMe: Boolean,
    isLocating: Boolean,
    locationError: Boolean,
    userLocation: GeoLocation?,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    resultsViewModel: ResultsViewModel,
) {
    LoadHomePageScreen(
        modifier = modifier,
        contentScreen = contentScreen,
        isUserLoggedIn = isUserLoggedIn,
        closeToMe = closeToMe,
        isLocating = isLocating,
        locationError = locationError,
        userLocation = userLocation,
        onSearchClick = onSearchClick,
        onFiltersOpened = onFiltersOpened,
        onRequestCurrentLocation = onRequestCurrentLocation,
        onLocationPermissionDenied = onLocationPermissionDenied,
        onCloseToMeDisabled = onCloseToMeDisabled,
        onClearLocationError = onClearLocationError,
        onRoomClick = onRoomClick,
        onPrevPage = onPrevPage,
        onNextPage = onNextPage,
        resultsViewModel = resultsViewModel
    )
}
