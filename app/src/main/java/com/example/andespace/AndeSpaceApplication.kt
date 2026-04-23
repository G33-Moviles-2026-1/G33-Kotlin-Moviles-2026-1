package com.example.andespace

import android.app.Application

class AndeSpaceApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        container.syncManager
    }
}