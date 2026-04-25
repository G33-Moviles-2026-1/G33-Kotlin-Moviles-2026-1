package com.example.andespace.ui.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.data.repository.RecommendationsRepository
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.RoomDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TimeWindowOut(val start: String, val end: String)
data class RoomSearchItemOut(
    val room_id: String,
    val room_number: String?,
    val building_name: String,
    val building_code: String,
    val capacity: Int,
    val matching_windows: List<TimeWindowOut> = emptyList(),
    val utilities: List<String> = emptyList(),
)
enum class InteractionAction { SKIP, FAVORITE, BOOK }

data class RecommendationsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStartTime: LocalTime? = null,
    val selectedEndTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val recommendations: List<RoomSearchItemOut> = emptyList(),
    val currentIndex: Int = 0,
    val favoriteIds: Set<String> = emptySet(),
    val error: String? = null,
    val isSearchActive: Boolean = false,
    val showBookingDialog: Boolean = false,
    val bookingPurpose: String = "",
    val isBookingInProgress: Boolean = false
) {
    val currentRoom: RoomSearchItemOut?
        get() = recommendations.getOrNull(currentIndex)
}

class RecommendationsViewModel(
    private val repository: RecommendationsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val bookingsRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    fun toggleFavorite(room: RoomDto, isCurrentlyFavorite: Boolean) {
        _uiState.update { state ->
            val updatedFavorites = if (isCurrentlyFavorite) {
                state.favoriteIds - room.id
            } else {
                state.favoriteIds + room.id
            }
            state.copy(favoriteIds = updatedFavorites)
        }

        onInteract(InteractionAction.FAVORITE, room.id)

        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                favoritesRepository.deleteFavorite(room.id)
            } else {
                favoritesRepository.addFavorite(room)
            }
        }
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


    fun openBookingDialog() {
        _uiState.update { it.copy(showBookingDialog = true, bookingPurpose = "") }
    }

    fun closeBookingDialog() {
        _uiState.update { it.copy(showBookingDialog = false, bookingPurpose = "") }
    }

    fun updateBookingPurpose(purpose: String) {
        _uiState.update { it.copy(bookingPurpose = purpose) }
    }

    fun confirmQuickBooking(room: RoomDto) {
        val state = _uiState.value
        if (state.selectedStartTime == null || state.selectedEndTime == null || state.bookingPurpose.isBlank()) return

        _uiState.update { it.copy(isBookingInProgress = true) }

        viewModelScope.launch {
            val exactWindow = state.currentRoom?.matching_windows?.firstOrNull()
            val finalStartTime = exactWindow?.start ?: state.selectedStartTime.format(timeFormatter)
            val finalEndTime = exactWindow?.end ?: state.selectedEndTime.format(timeFormatter)
            val request = CreateBookingRequest(
                roomId = room.id,
                date = state.selectedDate.toString(),
                startTime = finalStartTime,
                endTime = finalEndTime,
                purpose = state.bookingPurpose
            )

            bookingsRepository.createBooking(request)
                .onSuccess {
                    onInteract(InteractionAction.BOOK, room.id)
                    _uiState.update {
                        it.copy(
                            isBookingInProgress = false,
                            showBookingDialog = false,
                            currentIndex = it.currentIndex + 1
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isBookingInProgress = false) }
                }
        }
    }

    fun onInteract(action: InteractionAction, roomId: String? = null) {
        val state = _uiState.value
        val targetRoomId = roomId ?: state.currentRoom?.room_id ?: return
        viewModelScope.launch {
            try {
                repository.submitInteraction(targetRoomId, action, state.selectedDate, state.selectedStartTime!!)
            } catch (e: Exception) { }
        }

        if (action == InteractionAction.SKIP) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

}
