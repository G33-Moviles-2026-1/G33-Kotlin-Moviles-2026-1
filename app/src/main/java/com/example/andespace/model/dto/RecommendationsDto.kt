package com.example.andespace.model.dto

import com.example.andespace.ui.recommendations.InteractionAction

data class InteractionPayload(
    val room_id: String,
    val action: InteractionAction,
    val weekday: String,
    val slot_start: String
)