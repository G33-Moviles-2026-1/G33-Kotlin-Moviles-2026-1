package com.example.andespace.ui.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.data.repository.shared.RepositoryMessages
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
                isLoading = false,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            var wasOnline = NetworkMonitor.isOnline.value
            NetworkMonitor.isOnline.collect { isOnline ->
                if (isOnline && !wasOnline) {
                    repository.syncPendingFavoriteActions()
                    refreshFromBackend(force = true)
                }
                wasOnline = isOnline
            }
        }
    }

    fun refreshFromBackend(force: Boolean = false) {
        viewModelScope.launch {
            val localRooms = repository.getLocalFavorites()

            if (localRooms.isNotEmpty() && _uiState.value.favoriteRooms.isEmpty()) {
                _uiState.value = FavoritesUiState(
                    favoriteRooms = localRooms,
                    favoriteIds = localRooms.map { it.id }.toSet(),
                    isLoading = false,
                    errorMessage = null
                )
            }

            if (!force && localRooms.isNotEmpty()) {
                repository.syncPendingFavoriteActions()
                return@launch
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
                        isLoading = false,
                        errorMessage = null
                    )

                    // 2. Tell the repository to save it!
                    repository.saveLocalFavorites(merged)
                },
                onFailure = { e ->
                    Log.e(TAG, "refreshFromBackend -> backend failed: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = RepositoryMessages.FAVORITES_SYNC_FAILED
                        )
                    }
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

    fun removeFavoriteWithUndo(room: RoomDto) {
        if (room.id !in _uiState.value.favoriteIds) return

        _uiState.update { state ->
            state.copy(
                favoriteIds = state.favoriteIds - room.id,
                favoriteRooms = state.favoriteRooms.filter { it.id != room.id },
                pendingUndoRoom = room,
                undoEventId = state.undoEventId + 1
            )
        }
        persistFavorites()

        viewModelScope.launch {
            repository.deleteFavorite(room.id)
                .onSuccess { analyticsRepository.trackFavoriteEvent(room, added = false) }
        }
    }

    fun undoRemoveFavorite() {
        val room = _uiState.value.pendingUndoRoom ?: return
        val isAlreadyFavorite = room.id in _uiState.value.favoriteIds
        if (isAlreadyFavorite) {
            clearPendingUndo()
            return
        }

        _uiState.update { state ->
            state.copy(
                favoriteIds = state.favoriteIds + room.id,
                favoriteRooms = state.favoriteRooms + room,
                pendingUndoRoom = null
            )
        }
        persistFavorites()

        viewModelScope.launch {
            repository.addFavorite(room)
                .onSuccess { analyticsRepository.trackFavoriteEvent(room, added = true) }
        }
    }

    fun clearPendingUndo() {
        _uiState.update { it.copy(pendingUndoRoom = null) }
    }

    fun clearFavorites() {
        _uiState.value = FavoritesUiState(isLoading = false)
    }

    fun onFavoritesScreenOpened() {
        viewModelScope.launch {
            repository.syncPendingFavoriteActions()
            refreshFromBackend(force = true)
        }
    }

    fun onRoomDetailOpened() {
        viewModelScope.launch {
            repository.syncPendingFavoriteActions()
        }
    }

    fun onAppForegrounded() {
        viewModelScope.launch {
            repository.syncPendingFavoriteActions()
            refreshFromBackend(force = true)
        }
    }

    private fun persistFavorites() {
        val rooms = _uiState.value.favoriteRooms
        viewModelScope.launch {
            repository.saveLocalFavorites(rooms)
        }
    }
}
