package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.andespace.data.location.GeoLocation
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.model.dto.RoomDto

@Composable
fun MainHomePageScreen(
    modifier: Modifier = Modifier,
    contentScreen: ContentScreen,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    hasUploadedSchedule: Boolean,
    closeToMe: Boolean,
    isLocating: Boolean,
    locationError: Boolean,
    userLocation: GeoLocation?,
    lastSearchConfig: HomeSearchConfig,
    searchError: String?,
    rooms: List<RoomDto>,
    currentPage: Int,
    totalPages: Int,
    showingCachedResults: Boolean = false,
    showOfflinePlaceholder: Boolean = false,
    favoriteIds: Set<String> = emptySet(),
    onFavoriteClick: ((RoomDto) -> Unit)? = null,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
    onRequestCurrentLocation: () -> Unit,
    onLocationPermissionDenied: () -> Unit,
    onCloseToMeDisabled: () -> Unit,
    onClearLocationError: () -> Unit,
    onRoomClick: (RoomDto) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onAutoSearchClick: () -> Unit,
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
        lastSearchConfig = lastSearchConfig,
        searchError = searchError,
        rooms = rooms,
        currentPage = currentPage,
        totalPages = totalPages,
        showingCachedResults = showingCachedResults,
        showOfflinePlaceholder = showOfflinePlaceholder,
        favoriteIds = favoriteIds,
        onFavoriteClick = onFavoriteClick,
        onSearchClick = onSearchClick,
        onFiltersOpened = onFiltersOpened,
        onRequestCurrentLocation = onRequestCurrentLocation,
        onLocationPermissionDenied = onLocationPermissionDenied,
        onCloseToMeDisabled = onCloseToMeDisabled,
        onClearLocationError = onClearLocationError,
        onRoomClick = onRoomClick,
        onPrevPage = onPrevPage,
        onNextPage = onNextPage,
        modifier = modifier,
        onAutoSearchClick = onAutoSearchClick
    )
}
