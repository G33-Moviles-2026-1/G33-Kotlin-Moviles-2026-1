package com.example.andespace.ui.state

import com.example.andespace.model.AppDestinations

data class MainUiState(
    val isUserMenuExpanded: Boolean = false,
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val userName: String = "Cargando...",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = true
    val isLoading: Boolean = false,
    val user: String= "",
    val password: String= "",
    val errorMessage: String? = null
)
