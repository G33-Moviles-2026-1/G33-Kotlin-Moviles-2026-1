package com.example.andespace

import android.app.Application
import com.example.andespace.data.network.NetworkMonitor

class AndeSpaceApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.register(this, BuildConfig.API_BASE_URL)
        container = DefaultAppContainer(this)
        container.syncManager
    }
}