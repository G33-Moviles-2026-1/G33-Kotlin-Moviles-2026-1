package com.example.andespace.data.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar(private val context: Context) : CookieJar {

    private val COOKIE_KEY = stringPreferencesKey("session_cookie")
    private val memoryCookies = mutableMapOf<String, Cookie>()
    private var isLoadedFromDisk = false

    private fun ensureLoaded(url: HttpUrl) {
        if (isLoadedFromDisk) return

        val savedCookieString = runBlocking {
            context.dataStore.data.first()[COOKIE_KEY]
        }

        if (!savedCookieString.isNullOrEmpty()) {
            savedCookieString.split(";").forEach { pair ->
                val parts = pair.split("=")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val value = parts[1].trim()
                    memoryCookies[name] = Cookie.Builder()
                        .name(name)
                        .value(value)
                        .domain(url.host)
                        .path("/")
                        .build()
                }
            }
        }
        isLoadedFromDisk = true
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        ensureLoaded(url)

        for (cookie in cookies) {
            memoryCookies[cookie.name] = cookie
        }

        val cookieString = memoryCookies.values.joinToString(";") { "${it.name}=${it.value}" }

        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[COOKIE_KEY] = cookieString
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        ensureLoaded(url)
        return memoryCookies.values.toList()
    }

    fun clearCookies() {
        memoryCookies.clear()
        isLoadedFromDisk = true

        runBlocking {
            context.dataStore.edit { preferences ->
                preferences.remove(COOKIE_KEY)
            }
        }
    }
}