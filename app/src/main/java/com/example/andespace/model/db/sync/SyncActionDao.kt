package com.example.andespace.model.db.sync

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