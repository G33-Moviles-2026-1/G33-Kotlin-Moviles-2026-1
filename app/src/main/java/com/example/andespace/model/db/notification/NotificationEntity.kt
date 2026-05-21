package com.example.andespace.model.db.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.andespace.model.dto.NotificationDto

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val friendUsername: String,
    val roomId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val isRead: Boolean,
    val createdAt: String
)

fun NotificationDto.toEntity() = NotificationEntity(
    id = id,
    type = type,
    friendUsername = payload.friend_username,
    roomId = payload.room_id,
    date = payload.date,
    startTime = payload.start_time,
    endTime = payload.end_time,
    isRead = is_read,
    createdAt = created_at
)
