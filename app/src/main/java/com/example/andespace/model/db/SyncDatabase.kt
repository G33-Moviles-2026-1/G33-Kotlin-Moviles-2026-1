package com.example.andespace.model.db

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase

@SuppressLint("RestrictedApi")
@Database(
    entities = [PendingSyncAction::class, PendingAnalyticsEvent::class, FavoriteRoomEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncActionDao(): SyncActionDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun favoritesDao(): FavoritesDao
}