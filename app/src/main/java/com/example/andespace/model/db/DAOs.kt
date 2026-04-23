package com.example.andespace.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: PendingSyncAction): Long

    @Query("SELECT * FROM pending_sync_actions ORDER BY id ASC")
    suspend fun getAllPendingActions(): List<PendingSyncAction>

    @Query("DELETE FROM pending_sync_actions WHERE id = :actionId")
    suspend fun deleteAction(actionId: Int): Int

    @Query("SELECT * FROM pending_sync_actions WHERE actionType IN ('ADD_FAVORITE', 'DELETE_FAVORITE') ORDER BY id ASC")
    suspend fun getPendingFavoriteActions(): List<PendingSyncAction>

    @Query("DELETE FROM pending_sync_actions WHERE actionType IN ('ADD_FAVORITE', 'DELETE_FAVORITE') AND localClassId = :mutationKey")
    suspend fun deleteFavoriteActionsByMutationKey(mutationKey: String): Int
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

@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRoomEntity): Long

    @Query("SELECT * FROM favorite_rooms WHERE userKey = :userKey ORDER BY roomId ASC")
    suspend fun getFavoritesByUser(userKey: String): List<FavoriteRoomEntity>

    @Query("DELETE FROM favorite_rooms WHERE userKey = :userKey AND roomId = :roomId")
    suspend fun deleteFavoriteByRoomId(userKey: String, roomId: String): Int

    @Query("DELETE FROM favorite_rooms WHERE userKey = :userKey")
    suspend fun clearFavoritesForUser(userKey: String): Int
}

