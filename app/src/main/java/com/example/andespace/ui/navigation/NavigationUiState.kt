package com.example.andespace.ui.navigation

data class NavigationUiState(
    val fromClassroom: String? = null,
    val toClassroom: String? = null,
    val instructions: List<String> = emptyList(),
    val totalTimeSeconds: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
