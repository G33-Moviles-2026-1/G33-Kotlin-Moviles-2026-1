package com.example.andespace.ui.favorites

import com.example.andespace.model.dto.RoomDto

data class FavoritesUiState(
    val favoriteRooms: List<RoomDto> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)
