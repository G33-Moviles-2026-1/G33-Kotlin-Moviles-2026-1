package com.example.andespace.data.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, newCookies: List<Cookie>) {
        cookies.clear()
        cookies.addAll(newCookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }

    fun clearCookies() {
        cookies.clear()
    }

}