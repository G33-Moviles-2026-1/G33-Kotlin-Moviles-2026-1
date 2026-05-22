package com.example.andespace.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.AuthRepository
import com.example.andespace.data.repository.NotificationsRepository
import com.example.andespace.data.repository.ThemePreferencesRepository
import com.example.andespace.model.AppDestinations
import com.example.andespace.ui.notifications.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val notificationsRepository: NotificationsRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    init {
        observeSavedThemeMode()
        checkExistingSession()
        observeUnreadCount()
        observeNotifications()
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
                    authRepository.logout()
                }
            }
        }
    }

    private fun observeUnreadCount() {
        notificationsRepository?.let { repo ->
            viewModelScope.launch {
                repo.unreadCount.collect { count ->
                    _uiState.update { it.copy(unreadNotificationsCount = count) }
                }
            }
        }
    }

    private fun observeNotifications() {
        notificationsRepository?.let { repo ->
            viewModelScope.launch {
                repo.notifications.collect { dtos ->
                    val models = dtos.map { it.toUiModel() }
                    _uiState.update { it.copy(notifications = models) }
                }
            }
        }
    }

    fun toggleNotificationsPopup() {
        _uiState.update {
            it.copy(
                isNotificationsPopupExpanded = !it.isNotificationsPopupExpanded,
                isUserMenuExpanded = false
            )
        }
    }

    fun dismissNotificationsPopup() {
        _uiState.update { it.copy(isNotificationsPopupExpanded = false) }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            notificationsRepository?.markAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            notificationsRepository?.markAllAsRead()
        }
    }

    private fun observeSavedThemeMode() {
        viewModelScope.launch {
            themePreferencesRepository.observeThemeMode().collect { savedTheme ->
                _uiState.update { it.copy(themeMode = savedTheme) }
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
            if (!hasSessionLocally) {
                _uiState.update { it.copy(isLoggedIn = false) }
                return@launch
            }

            _uiState.update { it.copy(isLoggedIn = true) }

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
                    isUserMenuExpanded = false,
                    isNotificationsPopupExpanded = false
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

    fun requestLoginRequiredDialog() {
        _uiState.update {
            it.copy(
                showLoginRequiredDialog = true,
                isUserMenuExpanded = false
            )
        }
    }

    fun expandUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = true, isNotificationsPopupExpanded = false) }
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
                    currentDestination = AppDestinations.CLASSROOMS
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
        viewModelScope.launch {
            themePreferencesRepository.saveThemeMode(mode)
        }
    }

    fun setSensorDarkMode(isDark: Boolean) {
        _uiState.update { it.copy(sensorDarkMode = isDark) }
    }
}
