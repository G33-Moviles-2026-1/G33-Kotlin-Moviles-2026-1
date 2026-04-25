package com.example.andespace.model.db

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.andespace.model.db.booking.BookingDao
import com.example.andespace.model.db.booking.BookingEntity
import com.example.andespace.model.db.favorites.FavoriteRoomEntity
import com.example.andespace.model.db.favorites.FavoritesDao
import com.example.andespace.model.db.sync.AnalyticsDao
import com.example.andespace.model.db.sync.PendingAnalyticsEvent
import com.example.andespace.model.db.sync.PendingSyncAction
import com.example.andespace.model.db.sync.SyncActionDao

@SuppressLint("RestrictedApi")
@Database(
    entities = [PendingSyncAction::class, PendingAnalyticsEvent::class, FavoriteRoomEntity::class, BookingEntity::class],
    version = 5,
    exportSchema = false
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncActionDao(): SyncActionDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun favoritesDao(): FavoritesDao

    abstract fun bookingDao(): BookingDao
}