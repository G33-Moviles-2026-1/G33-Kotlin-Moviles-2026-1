package com.example.andespace.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.dataStore
import com.example.andespace.model.navigation.NavigationSnapshot
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class NavigationPersistenceDataSource(
    private val context: Context,
    private val gson: Gson
) {
    private val snapshotKey = stringPreferencesKey("navigation_snapshot_json")

    suspend fun readSnapshot(): NavigationSnapshot? = withContext(Dispatchers.IO) {
        val snapshotJson = context.dataStore.data.first()[snapshotKey] ?: return@withContext null
        runCatching {
            gson.fromJson(snapshotJson, NavigationSnapshot::class.java)
        }.getOrNull()
    }

    suspend fun persistSnapshot(snapshot: NavigationSnapshot) = withContext(Dispatchers.IO) {
        context.dataStore.edit { preferences ->
            preferences[snapshotKey] = gson.toJson(snapshot)
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        context.dataStore.edit { preferences ->
            preferences.remove(snapshotKey)
        }
    }
}