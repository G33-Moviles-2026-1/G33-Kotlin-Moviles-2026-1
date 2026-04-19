package com.example.andespace.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.AuthRepository
import com.example.andespace.model.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    init {
        checkExistingSession()
        viewModelScope.launch {
            authRepository.observeSessionState().collect { hasValidCookie ->
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
                        authRepository.logout()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun logScreenChange(screenName: String) {
        viewModelScope.launch {
            analyticsRepository.trackScreensTime(screenName = screenName)
        }
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val hasSessionLocally = authRepository.hasLocalSession()
            if (hasSessionLocally) {
                _uiState.update { it.copy(isLoggedIn = true) }
            }

            val result = authRepository.getMeData()

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoggedIn = true) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: ""
                val isNetworkError = errorMsg.contains("internet", ignoreCase = true) ||
                        errorMsg.contains("network", ignoreCase = true)

                if (!isNetworkError) {
                    _uiState.update { it.copy(isLoggedIn = false) }
                    authRepository.logout()
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
                authRepository.logout()
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
}
