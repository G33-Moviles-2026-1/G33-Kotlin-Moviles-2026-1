package com.example.andespace.ui.detailRoom

import com.example.andespace.model.dto.RoomDto

data class DetailRoomUiState(
    val room: RoomDto? = null,
    val selectedDate: String? = null,
    val isLoadingAvailability: Boolean = false,
    val availabilityError: String? = null
)
