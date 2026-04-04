package com.example.andespace.ui.results

import com.example.andespace.model.dto.RoomDto

data class ResultsUiState(
    val isSearching: Boolean = false,
    val rooms: List<RoomDto> = emptyList(),
    val hasUploadedSchedule: Boolean = false,
    val errorMessage: String? = null,
    val selectedSearchDate: String? = null,
    val selectedRoom: RoomDto? = null,
    val resultsPageSize: Int = 20,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val showingCachedResults: Boolean = false
)
