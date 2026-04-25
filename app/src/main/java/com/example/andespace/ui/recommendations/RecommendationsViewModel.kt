package com.example.andespace.ui.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.data.repository.RecommendationsRepository
import com.example.andespace.model.dto.CreateBookingRequest
import com.example.andespace.model.dto.InteractionAction
import com.example.andespace.model.dto.RoomDto
import com.example.andespace.model.dto.TimeWindowOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class RecommendationsViewModel(
    private val repository: RecommendationsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val bookingsRepository: BookingRepository
) : ViewModel() {
    private var isFetchingMore = false
    private val _uiState = MutableStateFlow(RecommendationsUiState())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()
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

    fun nextRoom() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1

        _uiState.update { it.copy(currentIndex = nextIndex) }

        if (nextIndex >= _uiState.value.recommendations.size - 2) {
            fetchMoreRooms()
        }
    }

    private fun fetchMoreRooms() {
        if (isFetchingMore) return
        isFetchingMore = true

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val seenRoomIds = currentState.recommendations.map { it.room_id }

                val newResults = repository.getAutoSearchRecommendations(
                    targetDate = currentState.selectedDate.toString(),
                    targetTime = currentState.selectedTime.format(timeFormatter),
                    topK = 3,
                    excludeIds = seenRoomIds
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recommendations = it.recommendations + newResults
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            } finally {
                isFetchingMore = false
            }
        }
    }

    fun startAutoSearch() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, isSearchActive = true, error = null) }

        viewModelScope.launch {
            try {
                val localFavorites = favoritesRepository.getLocalFavorites()
                val currentFavoriteIds = localFavorites.map { it.id }.toSet()

                val results = repository.getAutoSearchRecommendations(
                    targetDate = state.selectedDate.toString(),
                    targetTime = state.selectedTime.format(timeFormatter),
                    topK = 3,
                    excludeIds = emptyList()
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recommendations = results,
                        currentIndex = 0,
                        favoriteIds = currentFavoriteIds
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }


    fun openBookingDialog() {
        val state = _uiState.value
        _uiState.update { it.copy(showBookingDialog = true, bookingPurpose = "") }
        val firstValidSlot = state.currentRoom?.matching_windows?.firstOrNull()
        _uiState.update {
            it.copy(
                showBookingDialog = true,
                bookingPurpose = "",
                bookingError = null,
                selectedBookingSlot = firstValidSlot
            )
        }
    }

    fun closeBookingDialog() {
        _uiState.update { it.copy(showBookingDialog = false, bookingPurpose = "") }
    }

    fun updateBookingPurpose(purpose: String) {
        _uiState.update { it.copy(bookingPurpose = purpose) }
    }

    fun updateBookingSlot(slot: TimeWindowOut) {
        _uiState.update { it.copy(selectedBookingSlot = slot, bookingError = null) }
    }

    fun confirmQuickBooking(room: RoomDto) {
        val state = _uiState.value
        val slot = state.selectedBookingSlot ?: return
        if (state.bookingPurpose.isBlank()) return
        _uiState.update { it.copy(isBookingInProgress = true, bookingError = null) }

        viewModelScope.launch {
            val request = CreateBookingRequest(
                roomId = room.id,
                date = state.selectedDate.toString(),
                startTime = slot.start,
                endTime = slot.end,
                purpose = state.bookingPurpose
            )

            bookingsRepository.createBooking(request)
                .onSuccess {
                    onInteract(InteractionAction.BOOK, room.id)
                    _uiState.update {
                        it.copy(
                            isBookingInProgress = false,
                            showBookingDialog = false,
                        )
                    }
                    nextRoom()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isBookingInProgress = false,
                            bookingError = error.message ?: "The booking could not be confirmed."
                        )
                    }
                }
        }
    }

    fun onInteract(action: InteractionAction, roomId: String? = null) {
        val state = _uiState.value
        val targetRoomId = roomId ?: state.currentRoom?.room_id ?: return
        viewModelScope.launch {
            try {
                repository.submitInteraction(
                    targetRoomId,
                    action,
                    targetDate = state.selectedDate,
                    slotStart = state.selectedTime
                )
            } catch (e: Exception) { }
        }

        if (action == InteractionAction.SKIP) {
            nextRoom()
        }
    }

}
