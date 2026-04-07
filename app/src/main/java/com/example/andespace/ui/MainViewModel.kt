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

class MainViewModel(application: Application) : AndroidViewModel(application){
    private val repository: AppRepository = AppRepository(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val result = repository.getMeData()

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoggedIn = true) }
            } else {
                _uiState.update { it.copy(isLoggedIn = false) }
            }
        }
    }

    fun onDestinationChanged(destination: AppDestinations) {
        _uiState.update { it.copy(currentDestination = destination, isUserMenuExpanded = false) }
        logScreenChange(destination.name)
    }

    fun expandUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = true) }
    }

    fun closeUserMenu() {
        _uiState.update { it.copy(isUserMenuExpanded = false) }
    }

    fun onLogOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.logout()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    isUserMenuExpanded = false,
                    isLoading = false,
                    currentDestination = AppDestinations.LOGIN
                )
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
