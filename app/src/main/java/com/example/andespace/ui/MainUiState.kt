package com.example.andespace.ui

import com.example.andespace.model.AppDestinations

data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val userName: String = "Loading...",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isUserMenuExpanded: Boolean = false
)
