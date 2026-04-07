package com.example.andespace.ui.favorites

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.dto.RoomDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository()
    private val gson = Gson()
    private val roomListType = object : TypeToken<List<RoomDto>>() {}.type

    companion object {
        private const val TAG = "FavoritesViewModel"
    }

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val localRooms = readFromDataStore()
            Log.d(TAG, "init -> loaded ${localRooms.size} rooms from DataStore")
            _uiState.value = FavoritesUiState(
                favoriteRooms = localRooms,
                favoriteIds = localRooms.map { it.id }.toSet(),
                isLoading = false
            )
        }
    }

    fun refreshFromBackend() {
        viewModelScope.launch {
            val localRooms = readFromDataStore()
            Log.d(TAG, "refreshFromBackend -> local cache has ${localRooms.size} rooms")
            if (localRooms.isNotEmpty() && _uiState.value.favoriteRooms.isEmpty()) {
                _uiState.value = FavoritesUiState(
                    favoriteRooms = localRooms,
                    favoriteIds = localRooms.map { it.id }.toSet(),
                    isLoading = false
                )
            }

            Log.d(TAG, "refreshFromBackend -> calling backend")
            repository.getMyFavorites().fold(
                onSuccess = { backendRooms ->
                    Log.d(TAG, "refreshFromBackend -> backend returned ${backendRooms.size} rooms: ${backendRooms.map { it.id }}")
                    val localById = localRooms.associateBy { it.id }
                    val merged = backendRooms.map { backendRoom ->
                        localById[backendRoom.id] ?: backendRoom
                    }
                    Log.d(TAG, "refreshFromBackend -> merged ${merged.size} rooms")
                    _uiState.value = FavoritesUiState(
                        favoriteRooms = merged,
                        favoriteIds = merged.map { it.id }.toSet(),
                        isLoading = false
                    )
                    persistToDataStore(merged)
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
        Log.d(TAG, "toggleFavorite -> roomId=${room.id}, wasAlreadyFavorite=$wasAlreadyFavorite")

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
                    .onSuccess {
                        Log.d(TAG, "deleteFavorite backend success: ${room.id}")
                        repository.trackFavoriteEvent(room, added = false)
                    }
                    .onFailure { Log.e(TAG, "deleteFavorite backend failed: ${room.id} -> ${it.message}") }
            } else {
                repository.addFavorite(room)
                    .onSuccess {
                        Log.d(TAG, "addFavorite backend success: ${room.id}")
                        repository.trackFavoriteEvent(room, added = true)
                    }
                    .onFailure { Log.e(TAG, "addFavorite backend failed: ${room.id} -> ${it.message}") }
            }
        }
    }

    fun clearFavorites() {
        Log.d(TAG, "clearFavorites -> clearing memory state")
        _uiState.value = FavoritesUiState(isLoading = false)
    }

    private fun persistFavorites() {
        val rooms = _uiState.value.favoriteRooms
        viewModelScope.launch { persistToDataStore(rooms) }
    }

    private suspend fun persistToDataStore(rooms: List<RoomDto>) {
        Log.d(TAG, "persistToDataStore -> saving ${rooms.size} rooms")
        val json = gson.toJson(rooms)
        getApplication<Application>().favoritesDataStore.edit { prefs ->
            prefs[FAVORITES_JSON_KEY] = json
        }
    }

    private suspend fun readFromDataStore(): List<RoomDto> {
        val prefs = getApplication<Application>().favoritesDataStore.data
            .catch { emit(emptyPreferences()) }
            .first()
        val json = prefs[FAVORITES_JSON_KEY]
        return if (!json.isNullOrBlank()) {
            runCatching<List<RoomDto>> { gson.fromJson(json, roomListType) }
                .getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }
}
