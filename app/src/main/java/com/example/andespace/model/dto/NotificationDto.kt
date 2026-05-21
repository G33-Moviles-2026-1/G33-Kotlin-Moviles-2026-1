package com.example.andespace.model.dto

data class NotificationsResponse(
    val total: Int,
    val unread: Int,
    val items: List<NotificationDto>
)

data class NotificationDto(
    val id: String,
    val type: String,
    val payload: BookingNotificationPayloadDto,
    val is_read: Boolean,
    val created_at: String
)

data class BookingNotificationPayloadDto(
    val friend_username: String,
    val room_id: String,
    val date: String,
    val start_time: String,
    val end_time: String
)
