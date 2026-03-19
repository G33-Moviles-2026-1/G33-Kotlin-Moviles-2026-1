package com.example.andespace.ui.homepage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.andespace.data.model.HomeSearchParams
import com.example.andespace.data.model.dto.RoomDto

@Composable
fun MainHomePageScreen(
    contentScreen: ContentScreen,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    hasUploadedSchedule: Boolean,
    searchError: String?,
    rooms: List<RoomDto>,
    currentPage: Int,
    totalPages: Int,
    onSearchClick: (HomeSearchParams) -> Unit,
    onFiltersOpened: () -> Unit,
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
        searchError = searchError,
        rooms = rooms,
        currentPage = currentPage,
        totalPages = totalPages,
        onSearchClick = onSearchClick,
        onFiltersOpened = onFiltersOpened,
        onRoomClick = onRoomClick,
        onPrevPage = onPrevPage,
        onNextPage = onNextPage,
        modifier = modifier
    )
}
