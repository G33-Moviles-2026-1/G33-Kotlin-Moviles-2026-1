package com.example.andespace.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.dataStore
import com.example.andespace.ui.main.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemePreferencesRepository(
    private val context: Context
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    fun observeThemeMode(): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            val saved = prefs[themeModeKey]
            saved.toThemeModeOrDefault()
        }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    private fun String?.toThemeModeOrDefault(): ThemeMode {
        return runCatching { ThemeMode.valueOf(this.orEmpty()) }
            .getOrDefault(ThemeMode.SYSTEM)
    }
}
