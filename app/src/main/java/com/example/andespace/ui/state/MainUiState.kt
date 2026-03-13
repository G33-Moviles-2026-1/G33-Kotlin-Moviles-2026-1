package com.example.andespace.ui.state

import com.example.andespace.model.AppDestinations

data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val userName: String = "Cargando...",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = true
)
