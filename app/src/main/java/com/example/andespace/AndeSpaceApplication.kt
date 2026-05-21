package com.example.andespace

import android.app.Application
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.network.OnlineRecoveryCoordinator

class AndeSpaceApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.register(this, BuildConfig.API_BASE_URL)
        container = DefaultAppContainer(this)
        OnlineRecoveryCoordinator.start(
            syncManager = container.syncManager,
            friendsRepository = container.friendsRepository,
            notificationsRepository = container.notificationsRepository,
            favoritesRepository = container.favoritesRepository
        )
    }
}