package com.example.andespace.data.repository

import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import com.example.andespace.data.network.ApiService
import com.example.andespace.model.dto.AutoSearchRequest
import com.example.andespace.model.dto.InteractionAction
import com.example.andespace.model.dto.InteractionPayload
import com.example.andespace.model.dto.RoomSearchItemOut
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class RecommendationsRepository(private val apiService: ApiService) {
    private val exactTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    suspend fun getAutoSearchRecommendations(
        targetDate: String,
        targetTime: String,
        topK: Int = 3,
        excludeIds: List<String> = emptyList()
    ): List<RoomSearchItemOut> {
        val requestBody = AutoSearchRequest(targetDate, targetTime, topK, excludeIds)
        return apiService.getAutoSearchRecommendations(requestBody)
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