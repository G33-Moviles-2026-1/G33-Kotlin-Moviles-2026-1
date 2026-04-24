package com.example.andespace.ui.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.RecommendationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class RoomSearchItemOut(val room_id: String, val building_name: String, val capacity: Int)
enum class InteractionAction { SKIP, FAVORITE, BOOK }

data class RecommendationsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStartTime: LocalTime? = null,
    val selectedEndTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val recommendations: List<RoomSearchItemOut> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null,
    val isSearchActive: Boolean = false
) {
    val currentRoom: RoomSearchItemOut?
        get() = recommendations.getOrNull(currentIndex)
}

class RecommendationsViewModel(
    private val repository: RecommendationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    fun updateDateSelection(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedStartTime = null, selectedEndTime = null) }
    }

    fun updateTimeSelection(start: LocalTime, end: LocalTime) {
        _uiState.update { it.copy(selectedStartTime = start, selectedEndTime = end) }
    }

    fun startAutoSearch() {
        val state = _uiState.value
        if (state.selectedStartTime == null || state.selectedEndTime == null) return

        _uiState.update { it.copy(isLoading = true, isSearchActive = true, error = null) }

        viewModelScope.launch {
            try {
                val results = repository.getAutoSearchRecommendations(
                    targetDate = state.selectedDate,
                    since = state.selectedStartTime,
                    until = state.selectedEndTime,
                    topK = 3
                )
                _uiState.update {
                    it.copy(isLoading = false, recommendations = results, currentIndex = 0)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun onInteract(action: InteractionAction) {
        val state = _uiState.value
        val room = state.currentRoom ?: return

        viewModelScope.launch {
            try {
                repository.submitInteraction(
                    roomId = room.room_id,
                    action = action,
                    targetDate = state.selectedDate,
                    slotStart = state.selectedStartTime ?: return@launch
                )
            } catch (e: Exception) {
            }
        }

        _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
    }

    fun resetSearch() {
        _uiState.update { it.copy(isSearchActive = false, recommendations = emptyList(), currentIndex = 0) }
    }
}