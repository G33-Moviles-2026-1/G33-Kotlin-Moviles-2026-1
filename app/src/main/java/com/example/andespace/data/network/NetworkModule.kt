package com.example.andespace.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    val cookieJar = SessionCookieJar()

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://g33-backend-moviles-2026-1.onrender.com")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}