package com.example.andespace.ui.state

import com.example.andespace.model.AppDestinations

/**
 * Pantalla de contenido actual dentro de la pestaña Classrooms.
 * - HOME: formulario de búsqueda ("Where do you want to go?").
 * - RESULTS: pantalla de resultados (tras pulsar Search).
 * - HISTORY: pantalla de historial (tras pulsar el botón reloj del header).
 */
enum class ContentScreen {
    HOME,
    RESULTS,
    HISTORY
}

/**
 * Representa el estado global de la UI.
 * Siguiendo MVVM, el ViewModel expone un solo objeto de estado para que la vista lo observe.
 */
data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val contentScreen: ContentScreen = ContentScreen.HOME,
    val userName: String = "Cargando...",
    val isLoading: Boolean = false
)
