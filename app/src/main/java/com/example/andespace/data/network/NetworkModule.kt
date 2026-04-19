package com.example.andespace.data.network

import com.example.andespace.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    val cookieJar = SessionCookieJar()

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(
            LocalhostFallbackInterceptor(
                primaryBaseUrl = BuildConfig.API_BASE_URL,
                fallbackBaseUrl = BuildConfig.API_FALLBACK_BASE_URL
            )
        )
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}