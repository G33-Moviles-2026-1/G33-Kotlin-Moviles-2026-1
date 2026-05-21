package com.example.andespace.ui.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.location.LocationSensor
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.model.HomeSearchParams
import com.example.andespace.ui.common.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomepageViewModel(
    private val analyticsRepository: AnalyticsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(HomepageUiState())
    val uiState: StateFlow<HomepageUiState> = _uiState.asStateFlow()

    private val _onNavigateToNavigation = MutableStateFlow<String?>(null)
    val onNavigateToNavigation: StateFlow<String?> = _onNavigateToNavigation.asStateFlow()

    fun onNavigateToNavigation(roomId: String) {
        _onNavigateToNavigation.value = roomId
    }

    fun onNavigationHandled() {
        _onNavigateToNavigation.value = null
    }

    fun resetToHome() {
        _uiState.update { it.copy(contentScreen = ContentScreen.HOME) }
    }

    fun onShowResults() {
        _uiState.update { it.copy(contentScreen = ContentScreen.RESULTS) }
    }

    fun cacheLastSearchConfig(params: HomeSearchParams) {
        _uiState.update { it.copy(lastSearchConfig = params.toSearchConfig()) }
    }

    fun onShowRoomDetailScreen() {
        _uiState.update { it.copy(contentScreen = ContentScreen.ROOM_DETAIL) }
    }

    fun onFiltersOpened() {
        viewModelScope.launch { analyticsRepository.trackHomeEvent("home_filters_opened") }
    }

    fun onShowMakeBooking() {
        _uiState.update { it.copy(contentScreen = ContentScreen.MAKE_BOOKING) }
    }

    fun onShowAutoSearch() {
        _uiState.update { it.copy(contentScreen = ContentScreen.AUTO_SEARCH) }
    }

    fun onBackPressedInSearchFlow(): Boolean {
        val current = _uiState.value.contentScreen
        val previous = when (current) {
            ContentScreen.MAKE_BOOKING -> ContentScreen.ROOM_DETAIL
            ContentScreen.ROOM_DETAIL -> ContentScreen.RESULTS
            ContentScreen.RESULTS -> ContentScreen.HOME
            ContentScreen.AUTO_SEARCH -> ContentScreen.HOME
            ContentScreen.HOME -> null
        }

        return if (previous != null) {
            _uiState.update { it.copy(contentScreen = previous) }
            true
        } else {
            false
        }
    }

    fun onCloseToMeDisabled() {
        _uiState.update {
            it.copy(
                closeToMe = false,
                isLocating = false,
                userLocation = null
            )
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                closeToMe = false,
                isLocating = false,
                userLocation = null
            )
        }
        SnackbarManager.showMessage(RepositoryMessages.LOCATION_FAILED)
    }

    fun requestCurrentLocation(locationSensor: LocationSensor) {
        _uiState.update {
            it.copy(
                closeToMe = true,
                isLocating = true
            )
        }

        viewModelScope.launch {
            val location = locationSensor.getCurrentLocation()
            if (location != null) {
                _uiState.update {
                    it.copy(
                        closeToMe = true,
                        isLocating = false,
                        userLocation = location
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        closeToMe = false,
                        isLocating = false,
                        userLocation = null
                    )
                }
                SnackbarManager.showMessage(RepositoryMessages.LOCATION_FAILED)
            }
        }
    }
}
