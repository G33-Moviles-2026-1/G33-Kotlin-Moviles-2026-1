package com.example.andespace.ui.schedule

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
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
}