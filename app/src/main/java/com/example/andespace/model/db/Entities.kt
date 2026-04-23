package com.example.andespace.model.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_actions")
data class PendingSyncAction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String,
    val payload: String,
    val localClassId: String? = null
)

@Entity(tableName = "pending_analytics_events")
data class PendingAnalyticsEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val eventDataJson: String,
    val timestamp: Long
)

@Entity(
    tableName = "favorite_rooms",
    indices = [Index(value = ["userKey", "roomId"], unique = true)]
)
data class FavoriteRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userKey: String,
    val roomId: String,
    val name: String?,
    val building: String?,
    val buildingCode: String?,
    val capacity: Int?,
    val utilitiesJson: String
)