package com.example.andespace.ui.favorites

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

val FAVORITES_JSON_KEY = stringPreferencesKey("favorite_rooms_json")
