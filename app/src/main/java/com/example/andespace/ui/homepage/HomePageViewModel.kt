package com.example.andespace.ui.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.location.LocationSensor
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomepageViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomepageUiState())
    val uiState: StateFlow<HomepageUiState> = _uiState.asStateFlow()

    fun resetToHome() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HOME) }
    }

    fun onShowResults() {
        _uiState.update { it.copy(contentScreen = ContentScreen.RESULTS) }
    }

    fun onShowRoomDetailScreen() {
        _uiState.update { it.copy(contentScreen = ContentScreen.ROOM_DETAIL) }
    }

    fun onFiltersOpened() {
        viewModelScope.launch { repository.trackHomeEvent("home_filters_opened") }
    }

    fun onShowMakeBooking() {
        _uiState.update { it.copy(contentScreen = ContentScreen.MAKE_BOOKING) }
    }

    fun onBackPressedInSearchFlow(): Boolean {
        val current = uiState.value.contentScreen
        val previous = when (current) {
            ContentScreen.MAKE_BOOKING -> ContentScreen.ROOM_DETAIL
            ContentScreen.ROOM_DETAIL -> ContentScreen.RESULTS
            ContentScreen.RESULTS -> ContentScreen.HOME
            ContentScreen.HOME -> null
        }

        return if (previous != null) {
            _uiState.update { it.copy(contentScreen = previous) }
            true
        } else {
            false
        }
    }

    fun clearLocationError() {
        _uiState.update { it.copy(locationError = false) }
    }

    fun onCloseToMeDisabled() {
        _uiState.update {
            it.copy(
                closeToMe = false,
                isLocating = false,
                locationError = false,
                userLocation = null
            )
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                closeToMe = false,
                isLocating = false,
                locationError = true,
                userLocation = null
            )
        }
    }

    fun requestCurrentLocation(locationSensor: LocationSensor) {
        _uiState.update {
            it.copy(
                closeToMe = true,
                isLocating = true,
                locationError = false
            )
        }

        viewModelScope.launch {
            val location = locationSensor.getCurrentLocation()
            if (location != null) {
                _uiState.update {
                    it.copy(
                        closeToMe = true,
                        isLocating = false,
                        locationError = false,
                        userLocation = location
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        closeToMe = false,
                        isLocating = false,
                        locationError = true,
                        userLocation = null
                    )
                }
            }
        }
    }
}
