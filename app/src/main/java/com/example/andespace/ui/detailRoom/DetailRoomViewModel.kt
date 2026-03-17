package com.example.andespace.ui.detailRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.model.dto.RoomDto
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailRoomViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailRoomUiState())
    val uiState: StateFlow<DetailRoomUiState> = _uiState.asStateFlow()

    fun setRoom(room: RoomDto, selectedDate: String? = null) {
        val dateValue = selectedDate ?: _uiState.value.selectedDate ?: currentDateApiValue()
        _uiState.update {
            it.copy(
                room = room,
                selectedDate = dateValue,
                isLoadingAvailability = true,
                availabilityError = null
            )
        }
        fetchRoomAvailability(roomId = room.id, dateValue = dateValue)
    }

    fun onDateChange(dateValue: String) {
        val roomId = _uiState.value.room?.id ?: return
        _uiState.update {
            it.copy(
                selectedDate = dateValue,
                isLoadingAvailability = true,
                availabilityError = null
            )
        }
        fetchRoomAvailability(roomId = roomId, dateValue = dateValue)
    }

    private fun fetchRoomAvailability(roomId: String, dateValue: String) {
        viewModelScope.launch {
            repository.getRoomAvailability(roomId = roomId, dateValue = dateValue)
                .fold(
                    onSuccess = { windows ->
                        _uiState.update { state ->
                            val currentRoom = state.room
                            if (currentRoom == null || currentRoom.id != roomId) {
                                state.copy(
                                    isLoadingAvailability = false,
                                    availabilityError = null
                                )
                            } else {
                                state.copy(
                                    room = currentRoom.copy(matchingWindows = windows),
                                    isLoadingAvailability = false,
                                    availabilityError = null
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingAvailability = false,
                                availabilityError = error.message ?: "Could not load availability"
                            )
                        }
                    }
                )
        }
    }

    private fun currentDateApiValue(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }
}
