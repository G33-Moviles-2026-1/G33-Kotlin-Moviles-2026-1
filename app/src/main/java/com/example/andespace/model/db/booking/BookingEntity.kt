package com.example.andespace.model.db.booking

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.andespace.model.dto.BookingDto

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val purpose: String,
    val status: String,
    val createdAt: String?
)

fun BookingDto.toEntity() = BookingEntity(
    id = id,
    roomId = roomId,
    date = date,
    startTime = startTime,
    endTime = endTime,
    purpose = purpose,
    status = status,
    createdAt = createdAt
)

fun BookingEntity.toDto() = BookingDto(
    id = id,
    roomId = roomId,
    date = date,
    startTime = startTime,
    endTime = endTime,
    purpose = purpose,
    status = status,
    createdAt = createdAt
)
