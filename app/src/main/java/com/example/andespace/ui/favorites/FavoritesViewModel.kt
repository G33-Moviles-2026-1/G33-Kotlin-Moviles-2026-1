package com.example.andespace.ui.favorites

import androidx.lifecycle.ViewModel
import com.example.andespace.model.dto.RoomDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavoritesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun toggleFavorite(room: RoomDto) {
        _uiState.update { state ->
            if (room.id in state.favoriteIds) {
                state.copy(
                    favoriteIds = state.favoriteIds - room.id,
                    favoriteRooms = state.favoriteRooms.filter { it.id != room.id }
                )
            } else {
                state.copy(
                    favoriteIds = state.favoriteIds + room.id,
                    favoriteRooms = state.favoriteRooms + room
                )
            }
        }
    }

    fun clearFavorites() {
        _uiState.value = FavoritesUiState()
    }
}
