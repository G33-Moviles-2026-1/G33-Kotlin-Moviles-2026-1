package com.example.andespace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import com.example.andespace.model.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MainViewModel(application: Application) : AndroidViewModel(application){
    private val repository: AppRepository = AppRepository(application)
    private val _uiState = MutableStateFlow(MainUiState())
    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    init {
        checkExistingSession()
        viewModelScope.launch {
            repository.observeSessionState().collect { hasValidCookie ->
                if (!hasValidCookie && _uiState.value.isLoggedIn) {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = false,
                            isUserMenuExpanded = false,
                            isLoading = false,
                            currentDestination = AppDestinations.LOGIN,
                            showSessionExpiredDialog = true
                        )
                    }
                    try {
                        repository.logout()
                    } catch (_: Exception) {
                    }
                }
            }
        }
        viewModelScope.launch {
            AppRepository.networkStateEvent.collect { isOnline ->
                val message = if (isOnline) {
                    "Connection established."
                } else {
                    "No internet connection. Viewing stored information."
                }
                _snackbarEvent.send(message)
            }
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val hasSessionLocally = repository.hasLocalSession()
            if (hasSessionLocally) {
                _uiState.update { it.copy(isLoggedIn = true) }
            }

            val result = repository.getMeData()

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoggedIn = true) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: ""
                val isNetworkError = errorMsg.contains("internet", ignoreCase = true) ||
                        errorMsg.contains("network", ignoreCase = true)

                if (!isNetworkError) {
                    _uiState.update { it.copy(isLoggedIn = false) }
                    repository.logout()
                }
            }
        }
    }

    fun onDestinationChanged(destination: AppDestinations) {
        val requiresAuth = destination in listOf(
            AppDestinations.FAVORITES,
            AppDestinations.BOOKINGS,
            AppDestinations.SCHEDULE
        )
        if (requiresAuth && !_uiState.value.isLoggedIn) {
            _uiState.update {
                it.copy(
                    showLoginRequiredDialog = true,
                    isUserMenuExpanded = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentDestination = destination,
                    isUserMenuExpanded = false
                )
            }
            logScreenChange(destination.name)
        }
    }

    fun dismissSessionExpiredDialog() {
        _uiState.update { it.copy(showSessionExpiredDialog = false) }
    }

    fun dismissLoginRequiredDialog(navigateToLogin: Boolean) {
        _uiState.update { it.copy(showLoginRequiredDialog = false) }

        if (navigateToLogin) {
            _uiState.update { it.copy(currentDestination = AppDestinations.LOGIN) }
            logScreenChange(AppDestinations.LOGIN.name)
        } else {
            onDestinationChanged(AppDestinations.CLASSROOMS)
        }
    }


    fun expandUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = true) }
    }

    fun closeUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = false) }
    }

    fun onLogOut() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    isUserMenuExpanded = false,
                    isLoading = false,
                    currentDestination = AppDestinations.LOGIN
                )
            }

            try {
                repository.logout()
            } catch (_: Exception) {

            }
        }
    }

    fun onLogin() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setSensorDarkMode(isDark: Boolean) {
        _uiState.update { it.copy(sensorDarkMode = isDark) }
    }

    private fun logScreenChange(screenName: String) {
        viewModelScope.launch {
            repository.trackScreensTime(
                screenName = screenName
            )
        }
    }
}
