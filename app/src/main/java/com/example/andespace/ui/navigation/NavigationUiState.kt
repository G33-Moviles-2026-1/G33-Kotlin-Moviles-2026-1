package com.example.andespace.ui.navigation

data class NavigationUiState(
    val fromClassroom: String? = null,
    val toClassroom: String? = null,
    val instructions: List<String> = emptyList(),
    val totalTimeSeconds: Int = 0,
    val origin: String? = null,
    val destination: String? = null,
    val steps: List<String> = emptyList(),
    val estimatedTimeSeconds: Int = 0,
    val isLoading: Boolean = false,
    val isLocating: Boolean = false,
    val isFromCache: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isUsingGpsOrigin: Boolean = false,
    val error: String? = null
)
