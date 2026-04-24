package com.example.andespace.model.db.sync

import androidx.room.Entity
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