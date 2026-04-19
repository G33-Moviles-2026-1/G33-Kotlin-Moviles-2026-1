package com.example.andespace.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.LoginRequest
import com.example.andespace.data.network.RegisterRequest
import com.example.andespace.data.network.SessionCookieJar
import com.example.andespace.data.network.dataStore
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.extractErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class AuthRepository(private val apiService: ApiService, private val context: Context) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    fun observeSessionState(): Flow<Boolean> {
        val cookieKey = stringPreferencesKey("session_cookie")
        return context.dataStore.data.map { preferences ->
            val cookie = preferences[cookieKey]
            !cookie.isNullOrEmpty()
        }
    }


    suspend fun register(email: String, password: String, semester: String): Result<Boolean> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, semester))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (_: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun getMeData(): Result<String> {
        return try {
            val response = apiService.checkSession()
            if (response.isSuccessful) {
                val dataString = response.body().toString()
                Result.success(dataString)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMeData offline error: ${e.message}", e)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }


    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage =
                    extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (_: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            apiService.logout()
            SessionCookieJar(context).clearCookies()

            Result.success(true)
        } catch (_: Exception) {
            SessionCookieJar(context).clearCookies()
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }

    suspend fun hasLocalSession(): Boolean {
        return try {
            val cookieKey = stringPreferencesKey("session_cookie")
            val savedCookieString = context.dataStore.data.first()[cookieKey]
            !savedCookieString.isNullOrEmpty()
        } catch (_: Exception) {
            false
        }
    }

}