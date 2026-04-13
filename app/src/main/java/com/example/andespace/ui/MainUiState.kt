package com.example.andespace.ui

import com.example.andespace.model.AppDestinations

enum class ThemeMode { AUTOMATIC, SYSTEM, LIGHT, DARK }

data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val userName: String = "Loading...",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isUserMenuExpanded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val sensorDarkMode: Boolean = false,
    val showLoginRequiredDialog: Boolean = false,
    val showSessionExpiredDialog: Boolean = false
)
