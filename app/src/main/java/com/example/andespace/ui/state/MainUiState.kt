package com.example.andespace.ui.state

import com.example.andespace.data.api.dto.RoomDto
import com.example.andespace.model.AppDestinations

enum class ContentScreen {
    HOME,
    RESULTS,
    HISTORY
}

data class MainUiState(
    val currentDestination: AppDestinations = AppDestinations.CLASSROOMS,
    val contentScreen: ContentScreen = ContentScreen.HOME,
    val userName: String = "Cargando...",
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<RoomDto> = emptyList(),
    val searchError: String? = null
)
