package com.example.andespace.ui.state

import com.example.andespace.model.AppDestinations

/**
 * Representa el estado global de la UI.
 * Siguiendo MVVM, el ViewModel expone un solo objeto de estado para que la vista lo observe.
 */
data class MainUiState(
    val isUserMenuExpanded: Boolean = false,
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val isLoading: Boolean = false,
    val user: String= "",
    val password: String= "",
    val errorMessage: String? = null
)
