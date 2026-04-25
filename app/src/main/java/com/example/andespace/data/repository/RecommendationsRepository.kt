package com.example.andespace.data.repository

import com.example.andespace.data.network.ApiService
import com.example.andespace.model.dto.InteractionPayload
import com.example.andespace.ui.recommendations.InteractionAction
import com.example.andespace.ui.recommendations.RoomSearchItemOut
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class RecommendationsRepository(private val apiService: ApiService) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val exactTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    suspend fun getAutoSearchRecommendations(
        targetDate: LocalDate,
        since: LocalTime,
        until: LocalTime,
        topK: Int = 3
    ): List<RoomSearchItemOut> {
        val dateString = targetDate.format(dateFormatter)
        val sinceString = since.format(timeFormatter)
        val untilString = until.format(timeFormatter)

        return apiService.getAutoSearchRecommendations(
            targetDate = dateString,
            since = sinceString,
            until = untilString,
            topK = topK
        )
    }

    suspend fun submitInteraction(
        roomId: String,
        action: InteractionAction,
        targetDate: LocalDate,
        slotStart: LocalTime
    ) {
        val weekday = targetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()
        val timeString = slotStart.format(exactTimeFormatter)

        val payload = InteractionPayload(
            room_id = roomId,
            action = action,
            weekday = weekday,
            slot_start = timeString
        )

        apiService.submitRoomInteraction(payload)
    }
}