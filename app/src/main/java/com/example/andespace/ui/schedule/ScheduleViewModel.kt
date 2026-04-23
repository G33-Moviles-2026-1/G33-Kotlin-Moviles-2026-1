package com.example.andespace.ui.schedule

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.dto.ManualClassIn
import com.example.andespace.model.dto.ManualScheduleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()
    private var currentWeekDate: LocalDate = LocalDate.now()

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

            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val dateString = currentWeekDate.format(formatter)
                val schedule = repository.getWeeklySchedule(dateString)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSchedule = true,
                        scheduleData = schedule
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load schedule.")
                }
            }
        }
    }

    fun loadNextWeek() {
        currentWeekDate = currentWeekDate.plusDays(7)
        loadSchedule()
    }

    fun loadPreviousWeek() {
        currentWeekDate = currentWeekDate.minusDays(7)
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

            try {
                val existingClassesResponse = repository.getScheduleClasses()

                val existingClassesToUpload = existingClassesResponse.classes.map { oldClass ->
                    ManualClassIn(
                        title = oldClass.title ?: "Unknown Class",
                        location_text = oldClass.location_text,
                        room_id = oldClass.room_id,
                        start_date = oldClass.start_date,
                        end_date = oldClass.end_date,
                        start_time = oldClass.start_time,
                        end_time = oldClass.end_time,
                        weekdays = oldClass.weekdays
                    )
                }

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

                val combinedClasses = existingClassesToUpload + newClass
                val payload = ManualScheduleIn(classes = combinedClasses)

                repository.uploadManualSchedule(payload)

                _uiState.update {
                    it.copy(
                        isLoading = false, classTitle = "", classRoom = "",
                        startDate = "", endDate = "", startTime = "",
                        endTime = "", selectedDays = emptySet()
                    )
                }
                onSuccess()

            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to create class. Server error.")
                }
            }
        }
    }

    fun deleteSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                repository.deleteSchedule()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSchedule = false,
                        scheduleData = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete schedule. Please try again."
                    )
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

    fun resetToCurrentWeek() {
        currentWeekDate = LocalDate.now()
        loadSchedule()
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                repository.deleteClass(classId)
                loadSchedule()

            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete class. Please try again."
                    )
                }
            }
        }
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

    fun hideRecommendations() {
        _uiState.update { it.copy(isShowingRecommendations = false, recommendationsData = null) }
    }
}