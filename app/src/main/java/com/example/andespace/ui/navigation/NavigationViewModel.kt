package com.example.andespace.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.location.LocationSensor
import com.example.andespace.data.repository.NavigationRepository
import com.example.andespace.model.dto.NavigationPathSearchParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val repository: NavigationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    fun onFromClassroomChange(fromClassroom: String) {
        _uiState.update { it.copy(fromClassroom = fromClassroom) }
    }

    fun onToClassroomChange(toClassroom: String) {
        _uiState.update { it.copy(toClassroom = toClassroom) }
    }

    fun getInstructions(initClassroom: String, endClassroom: String) {
        if (initClassroom.isBlank() || endClassroom.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = repository.getNavigationPath(
                NavigationPathSearchParams(
                    fromClassroom = initClassroom,
                    toClassroom = endClassroom
                )
            )
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        fromClassroom = response.fromClassroom,
                        toClassroom = response.toClassroom,
                        instructions = response.steps,
                        totalTimeSeconds = response.totalTime,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    fun useCurrentLocationAsFromClassroom(locationSensor: LocationSensor) {
        _uiState.update { it.copy(isLocating = true, error = null) }

        viewModelScope.launch {
            val location = locationSensor.getCurrentLocation()
            if (location == null) {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        error = "Could not get your current location."
                    )
                }
                return@launch
            }

            val result = repository.getNearestNavigationNode(location.latitude, location.longitude)
            result.onSuccess { nearestNode ->
                _uiState.update {
                    it.copy(
                        fromClassroom = nearestNode.buildingCode,
                        isLocating = false,
                        error = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        error = error.message ?: "Could not determine your nearest location"
                    )
                }
            }
        }
    }
}
