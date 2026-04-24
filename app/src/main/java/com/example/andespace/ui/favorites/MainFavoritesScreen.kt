package com.example.andespace.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.andespace.model.dto.RoomDto

@Composable
fun MainFavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    onRoomClick: (RoomDto) -> Unit
) {
    val uiState by favoritesViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastShownError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        favoritesViewModel.onFavoritesScreenOpened()
    }

    LaunchedEffect(uiState.undoEventId) {
        val room = uiState.pendingUndoRoom ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${room.id} removed from favorites",
            actionLabel = "Undo",
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            favoritesViewModel.undoRemoveFavorite()
        } else {
            favoritesViewModel.clearPendingUndo()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        if (message == lastShownError) return@LaunchedEffect
        lastShownError = message
        snackbarHostState.showSnackbar(
            message = message,
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LoadFavoritesScreen(
                favoriteRooms = uiState.favoriteRooms,
                favoriteIds = uiState.favoriteIds,
                onRemoveFavorite = { room -> favoritesViewModel.removeFavoriteWithUndo(room) },
                onRoomClick = onRoomClick
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
