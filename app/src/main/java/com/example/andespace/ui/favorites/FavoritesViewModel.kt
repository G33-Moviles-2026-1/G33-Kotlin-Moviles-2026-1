package com.example.andespace.ui.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.model.dto.RoomDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FavoritesViewModel"
    }

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val localRooms = repository.getLocalFavorites()
            Log.d(TAG, "init -> loaded ${localRooms.size} rooms from local cache")

            _uiState.value = FavoritesUiState(
                favoriteRooms = localRooms,
                favoriteIds = localRooms.map { it.id }.toSet(),
                isLoading = false
            )
        }
    }

    fun refreshFromBackend() {
        viewModelScope.launch {
            val localRooms = repository.getLocalFavorites()

            if (localRooms.isNotEmpty() && _uiState.value.favoriteRooms.isEmpty()) {
                _uiState.value = FavoritesUiState(
                    favoriteRooms = localRooms,
                    favoriteIds = localRooms.map { it.id }.toSet(),
                    isLoading = false
                )
            }

            repository.getMyFavorites().fold(
                onSuccess = { backendRooms ->
                    val localById = localRooms.associateBy { it.id }
                    val merged = backendRooms.map { backendRoom ->
                        localById[backendRoom.id] ?: backendRoom
                    }

                    _uiState.value = FavoritesUiState(
                        favoriteRooms = merged,
                        favoriteIds = merged.map { it.id }.toSet(),
                        isLoading = false
                    )

                    // 2. Tell the repository to save it!
                    repository.saveLocalFavorites(merged)
                },
                onFailure = { e ->
                    Log.e(TAG, "refreshFromBackend -> backend failed: ${e.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    fun toggleFavorite(room: RoomDto) {
        val wasAlreadyFavorite = room.id in _uiState.value.favoriteIds

        _uiState.update { state ->
            if (wasAlreadyFavorite) {
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
        persistFavorites()

        viewModelScope.launch {
            if (wasAlreadyFavorite) {
                repository.deleteFavorite(room.id)
                    .onSuccess { analyticsRepository.trackFavoriteEvent(room, added = false) }
            } else {
                repository.addFavorite(room)
                    .onSuccess { analyticsRepository.trackFavoriteEvent(room, added = true) }
            }
        }
    }

    fun clearFavorites() {
        _uiState.value = FavoritesUiState(isLoading = false)
    }

    private fun persistFavorites() {
        val rooms = _uiState.value.favoriteRooms
        viewModelScope.launch {
            repository.saveLocalFavorites(rooms)
        }
    }
}
