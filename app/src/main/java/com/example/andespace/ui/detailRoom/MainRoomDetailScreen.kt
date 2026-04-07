package com.example.andespace.ui.detailRoom

import androidx.compose.runtime.Composable

@Composable
fun MainRoomDetailScreen(
    uiState: DetailRoomUiState,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onDateChange: (String) -> Unit,
    onBookRoom: () -> Unit
) {
    LoadRoomDetailScreen(
        room = uiState.room,
        selectedDate = uiState.selectedDate,
        isLoadingAvailability = uiState.isLoadingAvailability,
        availabilityError = uiState.availabilityError,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        onDateChange = onDateChange,
        onBookRoom = onBookRoom
    )
}
