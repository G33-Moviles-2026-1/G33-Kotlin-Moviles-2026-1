package com.example.andespace.ui.detailRoom

import androidx.compose.runtime.Composable

@Composable
fun MainRoomDetailScreen(
    uiState: DetailRoomUiState,
    onDateChange: (String) -> Unit,
    onBookRoom: () -> Unit
) {
    LoadRoomDetailScreen(
        room = uiState.room,
        selectedDate = uiState.selectedDate,
        isLoadingAvailability = uiState.isLoadingAvailability,
        availabilityError = uiState.availabilityError,
        onDateChange = onDateChange,
        onBookRoom = onBookRoom
    )
}
