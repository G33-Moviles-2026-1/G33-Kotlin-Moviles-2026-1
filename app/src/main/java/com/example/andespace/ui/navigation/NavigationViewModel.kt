package com.example.andespace.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.location.LocationSensor
import com.example.andespace.data.repository.NavigationRepository
import com.example.andespace.model.navigation.NavigationRoute
import com.example.andespace.ui.common.SnackbarManager
import com.example.andespace.ui.common.UserMessages
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

    init {
        restoreNavigationState()
    }

    fun onFromClassroomChange(fromClassroom: String) {
        _uiState.update { it.copy(fromClassroom = fromClassroom, isUsingGpsOrigin = false) }
    }

    fun onToClassroomChange(toClassroom: String) {
        _uiState.update { it.copy(toClassroom = toClassroom) }
    }

    fun getInstructions(initClassroom: String, endClassroom: String) {
        if (initClassroom.isBlank() || endClassroom.isBlank()) return

        _uiState.update {
            it.copy(
                isLoading = true,
                instructions = emptyList(),
                totalTimeSeconds = 0,
                isFromCache = false
            )
        }

        viewModelScope.launch {
            val result = repository.getRoute(
                origin = initClassroom,
                destination = endClassroom,
                isUsingGpsOrigin = _uiState.value.isUsingGpsOrigin
            )
            result.onSuccess { routeResult ->
                applyRouteResult(routeResult.route, routeResult.fromCache, routeResult.canGoBack, routeResult.canGoForward, routeResult.isUsingGpsOrigin)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                SnackbarManager.showMessage(error.message ?: UserMessages.UNKNOWN_ERROR)
            }
        }
    }

    fun useCurrentLocationAsFromClassroom(locationSensor: LocationSensor) {
        _uiState.update { it.copy(isLocating = true) }

        viewModelScope.launch {
            val result = repository.resolveOriginFromGpsIfNeeded(locationSensor)
            result.onSuccess { origin ->
                _uiState.update {
                    it.copy(
                        fromClassroom = origin,
                        origin = origin,
                        isUsingGpsOrigin = true,
                        isLocating = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLocating = false, isUsingGpsOrigin = false) }
                SnackbarManager.showMessage(error.message ?: UserMessages.LOCATION_RESOLVE_FAILED)
            }
        }
    }

    fun goBack() {
        viewModelScope.launch {
            repository.getRouteFromHistoryBack().onSuccess { routeResult ->
                val currentRoute = routeResult ?: return@onSuccess
                applyRouteResult(
                    route = currentRoute.route,
                    fromCache = currentRoute.fromCache,
                    canGoBack = currentRoute.canGoBack,
                    canGoForward = currentRoute.canGoForward,
                    isUsingGpsOrigin = currentRoute.isUsingGpsOrigin
                )
            }
        }
    }

    fun goForward() {
        viewModelScope.launch {
            repository.getRouteFromHistoryForward().onSuccess { routeResult ->
                val currentRoute = routeResult ?: return@onSuccess
                applyRouteResult(
                    route = currentRoute.route,
                    fromCache = currentRoute.fromCache,
                    canGoBack = currentRoute.canGoBack,
                    canGoForward = currentRoute.canGoForward,
                    isUsingGpsOrigin = currentRoute.isUsingGpsOrigin
                )
            }
        }
    }

    fun restoreNavigationState() {
        viewModelScope.launch {
            repository.restoreNavigationState().onSuccess { routeResult ->
                val currentRoute = routeResult ?: return@onSuccess
                applyRouteResult(
                    route = currentRoute.route,
                    fromCache = currentRoute.fromCache,
                    canGoBack = currentRoute.canGoBack,
                    canGoForward = currentRoute.canGoForward,
                    isUsingGpsOrigin = currentRoute.isUsingGpsOrigin
                )
            }
        }
    }

    private fun applyRouteResult(
        route: NavigationRoute,
        fromCache: Boolean,
        canGoBack: Boolean,
        canGoForward: Boolean,
        isUsingGpsOrigin: Boolean
    ) {
        _uiState.update {
            it.copy(
                fromClassroom = route.origin,
                toClassroom = route.destination,
                origin = route.origin,
                destination = route.destination,
                instructions = route.steps,
                steps = route.steps,
                totalTimeSeconds = route.totalTimeSeconds,
                estimatedTimeSeconds = route.totalTimeSeconds,
                isLoading = false,
                isFromCache = fromCache,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                isUsingGpsOrigin = isUsingGpsOrigin
            )
        }
    }
}
