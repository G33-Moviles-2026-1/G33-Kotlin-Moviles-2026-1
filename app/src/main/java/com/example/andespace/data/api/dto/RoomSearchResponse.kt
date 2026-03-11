package com.example.andespace.data.api.dto

data class RoomSearchResponse(
    val rooms: List<RoomDto> = emptyList()
)

data class RoomDto(
    val id: String? = null,
    val name: String? = null,
    val building: String? = null,
    val capacity: Int? = null
)
