package com.example.andespace.ui.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MainFavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    onRoomClick: (com.example.andespace.model.dto.RoomDto) -> Unit
) {
    val uiState by favoritesViewModel.uiState.collectAsState()

    LoadFavoritesScreen(
        favoriteRooms = uiState.favoriteRooms,
        favoriteIds = uiState.favoriteIds,
        onToggleFavorite = { room -> favoritesViewModel.toggleFavorite(room) },
        onRoomClick = onRoomClick
    )
}
