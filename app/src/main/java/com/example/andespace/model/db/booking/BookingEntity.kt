package com.example.andespace.model.db.booking

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.andespace.model.dto.BookingDto

enum class SyncStatus {
    SYNCED, PENDING_CREATE, PENDING_DELETE
}

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val purpose: String,
    val status: String,
    val createdAt: String?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

fun BookingDto.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED) = BookingEntity(
    id = id,
    roomId = roomId,
    date = date,
    startTime = startTime,
    endTime = endTime,
    purpose = purpose,
    status = status,
    createdAt = createdAt,
    syncStatus = syncStatus
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
