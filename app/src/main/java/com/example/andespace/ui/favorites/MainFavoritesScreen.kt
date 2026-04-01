package com.example.andespace.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.andespace.model.dto.RoomDto

@Composable
fun MainFavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    onRoomClick: (RoomDto) -> Unit
) {
    val uiState by favoritesViewModel.uiState.collectAsState()

    // Refresh from backend every time the user opens this screen.
    // Shows DataStore cache immediately, then updates with the backend result.
    LaunchedEffect(Unit) {
        favoritesViewModel.refreshFromBackend()
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LoadFavoritesScreen(
            favoriteRooms = uiState.favoriteRooms,
            favoriteIds = uiState.favoriteIds,
            onToggleFavorite = { room -> favoritesViewModel.toggleFavorite(room) },
            onRoomClick = onRoomClick
        )
    }
}
