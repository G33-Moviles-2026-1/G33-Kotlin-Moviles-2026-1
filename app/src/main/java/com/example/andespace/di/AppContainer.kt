package com.example.andespace.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.andespace.data.db.SyncActionDao
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.NetworkModule
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.AuthRepository
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.data.repository.RoomRepository
import com.example.andespace.data.repository.ScheduleRepository
import com.example.andespace.data.repository.SyncManager
import androidx.room.Room
import com.example.andespace.data.db.AnalyticsDao
import com.example.andespace.data.db.SyncDatabase
import com.google.gson.Gson

interface AppContainer {
    val apiService: ApiService
    val authRepository: AuthRepository
    val analyticsRepository: AnalyticsRepository
    val roomRepository: RoomRepository
    val scheduleRepository: ScheduleRepository
    val bookingRepository: BookingRepository
    val favoritesRepository: FavoritesRepository
    val syncManager: SyncManager
    val analyticsDao: AnalyticsDao
    val syncDao: SyncActionDao
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

    private val syncDatabase: SyncDatabase by lazy {
        Room.databaseBuilder(
            context,
            SyncDatabase::class.java,
            "andespace_sync_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    override val syncDao: SyncActionDao by lazy {
        syncDatabase.syncActionDao()
    }

    private val gson: Gson by lazy {
        Gson()
    }

    override val apiService: ApiService by lazy {
        NetworkModule.getApiService(context)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, context)
    }

    override val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepository(apiService, dataStore = context.favoritesDataStore)
    }

    override val analyticsDao: AnalyticsDao by lazy {
        syncDatabase.analyticsDao()
    }

    override val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(apiService, analyticsDao, gson)
    }

    override val roomRepository: RoomRepository by lazy {
        RoomRepository(apiService)
    }

    override val bookingRepository: BookingRepository by lazy {
        BookingRepository(apiService)
    }

    override val scheduleRepository: ScheduleRepository by lazy {
        ScheduleRepository(syncDao, apiService, context)
    }

    override val syncManager: SyncManager by lazy {
        SyncManager(syncDao, analyticsDao, scheduleRepository, apiService, gson)
    }
}