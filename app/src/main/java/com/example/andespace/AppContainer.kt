package com.example.andespace

import android.content.Context
import com.example.andespace.model.db.SyncActionDao
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.AnalyticsRepository
import com.example.andespace.data.repository.AuthRepository
import com.example.andespace.data.repository.BookingRepository
import com.example.andespace.data.repository.FavoritesRepository
import com.example.andespace.data.repository.RoomRepository
import com.example.andespace.data.repository.ScheduleRepository
import com.example.andespace.data.repository.SyncManager
import com.example.andespace.data.repository.ThemePreferencesRepository
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.andespace.model.db.AnalyticsDao
import com.example.andespace.model.db.FavoritesDao
import com.example.andespace.model.db.SyncDatabase
import com.example.andespace.data.network.AuthInterceptor
import com.example.andespace.data.network.SessionCookieJar
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val sessionCookieJar: SessionCookieJar
    val apiService: ApiService
    val authRepository: AuthRepository
    val analyticsRepository: AnalyticsRepository
    val roomRepository: RoomRepository
    val scheduleRepository: ScheduleRepository
    val bookingRepository: BookingRepository
    val favoritesRepository: FavoritesRepository
    val themePreferencesRepository: ThemePreferencesRepository
    val syncManager: SyncManager
    val analyticsDao: AnalyticsDao
    val syncDao: SyncActionDao
    val favoritesDao: FavoritesDao
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `favorite_rooms` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userKey` TEXT NOT NULL,
                    `roomId` TEXT NOT NULL,
                    `name` TEXT,
                    `building` TEXT,
                    `buildingCode` TEXT,
                    `capacity` INTEGER,
                    `utilitiesJson` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_favorite_rooms_userKey_roomId` ON `favorite_rooms` (`userKey`, `roomId`)"
            )
        }
    }

    private val syncDatabase: SyncDatabase by lazy {
        Room.databaseBuilder(
            context,
            SyncDatabase::class.java,
            "andespace_sync_database"
        )
            .addMigrations(migration2To3)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    override val syncDao: SyncActionDao by lazy {
        syncDatabase.syncActionDao()
    }


    private val gson: Gson by lazy {
        Gson()
    }


    override val sessionCookieJar: SessionCookieJar by lazy {
        SessionCookieJar(context.applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
            .addInterceptor(AuthInterceptor(sessionCookieJar))
            .build()
    }

    override val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(
            apiService = apiService,
            context = context,
            scheduleRepository = scheduleRepository,
            sessionCookieJar = sessionCookieJar
        )
    }

    override val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepository(
            apiService = apiService,
            favoritesDao = favoritesDao,
            syncActionDao = syncDao,
            context = context
        )
    }

    override val themePreferencesRepository: ThemePreferencesRepository by lazy {
        ThemePreferencesRepository(context)
    }

    override val analyticsDao: AnalyticsDao by lazy {
        syncDatabase.analyticsDao()
    }

    override val favoritesDao: FavoritesDao by lazy {
        syncDatabase.favoritesDao()
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