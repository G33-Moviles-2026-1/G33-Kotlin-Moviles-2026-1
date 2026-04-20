package com.example.andespace

import android.app.Application
import com.example.andespace.di.AppContainer
import com.example.andespace.di.DefaultAppContainer

class AndeSpaceApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        container.syncManager
    }
}