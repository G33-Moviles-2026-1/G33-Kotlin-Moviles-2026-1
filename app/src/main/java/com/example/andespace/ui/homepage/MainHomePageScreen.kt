package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.andespace.data.location.GeoLocation
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomDto

@Composable
fun MainHomePageScreen(
    contentScreen: ContentScreen,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    hasUploadedSchedule: Boolean,
    closeToMe: Boolean,
    isLocating: Boolean,
    locationError: Boolean,
    userLocation: GeoLocation?,
    searchError: String?,
    rooms: List<RoomDto>,
    currentPage: Int,
    totalPages: Int,
    showingCachedResults: Boolean = false,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    LoadHomePageScreen(
        contentScreen = contentScreen,
        isSearching = isSearching,
        isUserLoggedIn = isUserLoggedIn,
        hasUploadedSchedule = hasUploadedSchedule,
        closeToMe = closeToMe,
        isLocating = isLocating,
        locationError = locationError,
        userLocation = userLocation,
        searchError = searchError,
        rooms = rooms,
        currentPage = currentPage,
        totalPages = totalPages,
        showingCachedResults = showingCachedResults,
        onSearchClick = onSearchClick,
        onFiltersOpened = onFiltersOpened,
        onRequestCurrentLocation = onRequestCurrentLocation,
        onLocationPermissionDenied = onLocationPermissionDenied,
        onCloseToMeDisabled = onCloseToMeDisabled,
        onClearLocationError = onClearLocationError,
        onRoomClick = onRoomClick,
        onPrevPage = onPrevPage,
        onNextPage = onNextPage,
        modifier = modifier
    )
}
