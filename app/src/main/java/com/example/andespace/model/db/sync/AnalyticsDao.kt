package com.example.andespace.model.db.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnalyticsDao {
    @Insert
    suspend fun insertEvent(event: PendingAnalyticsEvent): Long

    @Query("SELECT * FROM pending_analytics_events ORDER BY timestamp ASC")
    suspend fun getAllPendingEvents(): List<PendingAnalyticsEvent>

    @Query("DELETE FROM pending_analytics_events WHERE id IN (:eventIds)")
    suspend fun deleteEvents(eventIds: List<Int>): Int

    @Query("""
        DELETE FROM pending_analytics_events 
        WHERE id NOT IN (
            SELECT id FROM pending_analytics_events 
            ORDER BY timestamp DESC 
            LIMIT 500
        )
    """)
    suspend fun pruneOldEvents()
}