package com.example.andespace.ui

import com.example.andespace.model.AppDestinations

data class MainUiState(
    val isUserMenuExpanded: Boolean = false,
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
)