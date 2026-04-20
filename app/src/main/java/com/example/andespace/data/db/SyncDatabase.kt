package com.example.andespace.data.db

import android.annotation.SuppressLint
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
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

@Dao
interface SyncActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: PendingSyncAction): Long

    @Query("SELECT * FROM pending_sync_actions ORDER BY id ASC")
    suspend fun getAllPendingActions(): List<PendingSyncAction>

    @Query("DELETE FROM pending_sync_actions WHERE id = :actionId")
    suspend fun deleteAction(actionId: Int): Int
}

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

@SuppressLint("RestrictedApi")
// 4. ADDED THE ENTITY AND BUMPED VERSION TO 2
@Database(
    entities = [PendingSyncAction::class, PendingAnalyticsEvent::class],
    version = 2,
    exportSchema = false
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncActionDao(): SyncActionDao
    abstract fun analyticsDao(): AnalyticsDao // 5. EXPOSE THE DAO
}