package com.example.andespace.ui.notifications

import com.example.andespace.model.dto.NotificationDto

fun NotificationDto.toUiModel() = NotificationUiModel(
    id = id,
    friendUsername = payload.friend_username,
    roomId = payload.room_id,
    date = payload.date,
    startTime = payload.start_time,
    endTime = payload.end_time,
    isRead = is_read
)

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationUiModel> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isOffline: Boolean = false
)

data class NotificationUiModel(
    val id: String,
    val friendUsername: String,
    val roomId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val isRead: Boolean
)
