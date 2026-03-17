package com.example.andespace.ui

import com.example.andespace.model.AppDestinations

enum class ContentScreen {
    HOME,
    RESULTS,
    HISTORY,
    ROOM_DETAIL
}

data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val contentScreen: ContentScreen = ContentScreen.HOME,
    val userName: String = "Loading...",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isUserMenuExpanded: Boolean = false
)
