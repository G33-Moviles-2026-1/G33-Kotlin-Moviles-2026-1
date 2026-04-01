package com.example.andespace.ui.schedule

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.schedule.ManualClassIn
import com.example.andespace.model.schedule.ManualScheduleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        checkScheduleStatus()
        if (uiState.value.hasSchedule){
            loadSchedule()
        }
    }

    fun checkScheduleStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.checkIfScheduleExists()

            result.onSuccess { hasSchedule ->
                if (hasSchedule) {
                    loadSchedule()
                } else {
                    _uiState.update { it.copy(isLoading = false, hasSchedule = false) }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Something went wrong. Please try again."
                    )
                }
            }
        }
    }
    fun uploadIcsFile(context: Context, uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.uploadIcs(context, uri)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, hasSchedule = true) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "The file could not be uploaded. Please try again."
                    )
                }
            }
        }
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getWeeklySchedule()

            result.onSuccess { data ->
                _uiState.update {
                    it.copy(isLoading = false,
                    hasSchedule = true,
                    scheduleData = data)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not load your schedule. Please try again.")
                }
            }
        }
    }

    fun clearScheduleData() {
        _uiState.update { ScheduleUiState() }
    }

    fun onClassTitleChange(title: String) {
        _uiState.update { it.copy(classTitle = title) }
    }

    fun onClassRoomChange(room: String) {
        _uiState.update { it.copy(classRoom = room) }
    }


    fun onStartDateChange(date: String) { _uiState.update { it.copy(startDate = date) } }
    fun onEndDateChange(date: String) { _uiState.update { it.copy(endDate = date) } }
    fun onStartTimeChange(time: String) { _uiState.update { it.copy(startTime = time) } }
    fun onEndTimeChange(time: String) { _uiState.update { it.copy(endTime = time) } }

    fun toggleWeekday(day: String) {
        _uiState.update { currentState ->
            val currentDays = currentState.selectedDays.toMutableSet()
            if (currentDays.contains(day)) {
                currentDays.remove(day)
            } else {
                currentDays.add(day)
            }
            currentState.copy(selectedDays = currentDays)
        }
    }


    fun uploadManualSchedule(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val manualClass = ManualClassIn(
                title = state.classTitle,
                room_id = state.classRoom.ifBlank { null },
                start_date = state.startDate.ifBlank { "2026-03-16" },
                end_date = state.endDate.ifBlank { "2026-06-16" },
                start_time = state.startTime.ifBlank { "08:00" },
                end_time = state.endTime.ifBlank { "09:30" },
                weekdays = state.selectedDays.toList()
            )

            val payload = ManualScheduleIn(classes = listOf(manualClass))


            try {

                //val result = repository.uploadManualSchedule(payload)

                _uiState.update { it.copy(isLoading = false, hasSchedule = true) }
                onSuccess()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to add class") }
            }
        }
    }

    fun showAddClassScreen() {
        _uiState.update { it.copy(isAddingManualClass = true) }
    }

    fun hideAddClassScreen() {
        _uiState.update { it.copy(isAddingManualClass = false) }
    }
}