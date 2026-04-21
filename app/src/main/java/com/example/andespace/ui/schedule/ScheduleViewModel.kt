package com.example.andespace.ui.schedule

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.ScheduleNotFoundException
import com.example.andespace.data.repository.ScheduleRepository
import com.example.andespace.model.dto.ManualClassIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


class ScheduleViewModel(
    private val repository: ScheduleRepository,
): ViewModel(){

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val hasCached = repository.hasAnyCachedSchedule()
            _uiState.update { it.copy(hasSchedule = hasCached) }
        }
        loadSchedule()
    }
    fun loadRecommendations(dateString: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val data = repository.getRoomRecommendationsForDay(dateString)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isShowingRecommendations = true,
                        recommendationsData = data
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load recommendations.")
                }
            }
        }
    }


    fun showAddClassScreen() {
        _uiState.update { it.copy(isAddingManualClass = true) }
    }

    fun hideAddClassScreen() {
        _uiState.update { it.copy(isAddingManualClass = false) }
    }

    fun hideRecommendations() {
        _uiState.update { it.copy(isShowingRecommendations = false, recommendationsData = null) }
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
                    it.copy(isLoading = false, errorMessage = error.message ?: "Something went wrong.")
                }
            }
        }
    }

    fun uploadIcsFile(context: Context, uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.uploadIcs(context, uri)

            result.onSuccess {
                repository.syncEntireScheduleFromBackend()

                _uiState.update { it.copy(isLoading = false, hasSchedule = true) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun forceRefreshScheduleFromBackend() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.syncEntireScheduleFromBackend()
            loadSchedule()
        }
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, scheduleData = null) }

            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val requestedDate = _uiState.value.currentWeekDate
                val expectedMonday = requestedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val dateString = expectedMonday.format(formatter)

                val schedule = repository.getWeeklySchedule(dateString)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSchedule = true,
                        scheduleData = schedule
                    )
                }

            } catch (e: ScheduleNotFoundException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSchedule = false,
                        scheduleData = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error loading schedule",
                        scheduleData = null
                    )
                }
            }
        }
    }

    fun loadNextWeek() {
        _uiState.update { it.copy(currentWeekDate = it.currentWeekDate.plusDays(7)) }
        loadSchedule()
    }

    fun loadPreviousWeek() {
        _uiState.update { it.copy(currentWeekDate = it.currentWeekDate.minusDays(7)) }
        loadSchedule()
    }

    fun resetToCurrentWeek() {
        _uiState.update {
            it.copy(
                currentWeekDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            )
        }
        loadSchedule()
    }

    fun clearScheduleData() {
        _uiState.update { ScheduleUiState() }
    }

    fun onClassTitleChange(title: String) {
        if(title.length <= 35) {
            _uiState.update { it.copy(classTitle = title) }
        }
    }

    fun onClassRoomChange(room: String) {
        if(room.length <= 20) {
            _uiState.update { it.copy(classRoom = room) }
        }
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

        if (state.classTitle.isBlank() || state.startDate.isBlank() ||
            state.endDate.isBlank() || state.startTime.isBlank() ||
            state.endTime.isBlank() || state.selectedDays.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all required fields.") }
            return
        }

        if (state.startDate > state.endDate) {
            _uiState.update { it.copy(errorMessage = "The end date cannot be before the start date.") }
            return
        }
        if (state.startTime >= state.endTime) {
            _uiState.update { it.copy(errorMessage = "The class must end after it starts.") }
            return
        }
        if (state.startTime < "05:30" || state.endTime > "22:00") {
            _uiState.update { it.copy(errorMessage = "Classes must be scheduled between 05:30 and 22:00.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val newClass = ManualClassIn(
                title = state.classTitle.trim(),
                location_text = null,
                room_id = state.classRoom.trim().ifBlank { null },
                start_date = state.startDate,
                end_date = state.endDate,
                start_time = "${state.startTime}:00",
                end_time = "${state.endTime}:00",
                weekdays = state.selectedDays.toList()
            )

            repository.uploadManualSchedule(newClass)

            _uiState.update {
                it.copy(
                    classTitle = "", classRoom = "", startDate = "", endDate = "",
                    startTime = "", endTime = "", selectedDays = emptySet(),
                    hasSchedule = true, isLoading = false
                )
            }
            loadSchedule()
            onSuccess()
        }
    }

    fun promptDeleteClass(classId: String) {
        _uiState.update { it.copy(classIdToDelete = classId) }
    }

    fun cancelDeleteClass() {
        _uiState.update { it.copy(classIdToDelete = null) }
    }

    fun confirmDeleteClass() {
        val classId = _uiState.value.classIdToDelete ?: return
        viewModelScope.launch {
            cancelDeleteClass() // Hide the dialog
            repository.deleteClass(classId)
            loadSchedule()
        }
    }

    fun promptDeleteSchedule() {
        _uiState.update { it.copy(showDeleteScheduleConfirm = true) }
    }

    fun cancelDeleteSchedule() {
        _uiState.update { it.copy(showDeleteScheduleConfirm = false) }
    }

    fun confirmDeleteSchedule() {
        viewModelScope.launch {
            cancelDeleteSchedule() // Hide the dialog
            repository.deleteSchedule()
            _uiState.update { it.copy(hasSchedule = false, scheduleData = null) }
        }
    }
}