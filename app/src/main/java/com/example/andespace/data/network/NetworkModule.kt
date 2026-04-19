package com.example.andespace.data.network

import android.content.Context
import com.example.andespace.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    @Volatile
    private var INSTANCE: ApiService? = null

    private fun provideOkHttpClient(context: Context): OkHttpClient {
        val sessionCookieJar = SessionCookieJar(context.applicationContext)
        val authInterceptor = AuthInterceptor(sessionCookieJar)

        return OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
            .addInterceptor(authInterceptor)
            .build()
    }

    fun getApiService(context: Context): ApiService {

        return INSTANCE ?: synchronized(this) {

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(provideOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val newInstance = retrofit.create(ApiService::class.java)
            INSTANCE = newInstance
            newInstance
        }
    }
}