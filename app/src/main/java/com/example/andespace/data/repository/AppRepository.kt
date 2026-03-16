package com.example.andespace.data.repository

import android.util.Log
import com.example.andespace.data.api.RetrofitClient
import com.example.andespace.data.api.dto.AnalyticsEventRequest
import com.example.andespace.data.api.dto.RoomSearchRequest
import com.example.andespace.data.api.dto.RoomSearchResponse
import com.example.andespace.data.model.HomeSearchParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.LoginRequest
import com.example.andespace.data.network.NetworkModule
import com.example.andespace.data.network.RegisterRequest
import org.json.JSONObject

class AppRepository(
    private val apiService: ApiService = NetworkModule.apiService
) {

    class ApiException(message: String) : Exception(message) {
        override val message: String
            get() = super.message?.removePrefix("java.lang.Exception: ")?.trim() ?: "Unknown API Error"
    }

    suspend fun register(email: String, password: String, semester: String): Result<Boolean> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, semester))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Check your connection"))
        }
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Check your connection"))
        }
    }

    suspend fun getMeData(): Result<String> {
        return try {
            val response = apiService.checkSession()
            if (response.isSuccessful) {
                val dataString = response.body().toString()
                Result.success(dataString)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (e: Exception) {
            Result.failure(ApiException("Network error: Check your connection"))
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            apiService.logout()
            NetworkModule.cookieJar.clearCookies()
            Result.success(true)
        } catch (e: Exception) {
            NetworkModule.cookieJar.clearCookies()
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: String?, defaultCode: Int): String {
        if (errorBody.isNullOrEmpty()) return "Error code: $defaultCode"

        return try {
            val jsonObject = JSONObject(errorBody)
            when {
                jsonObject.has("detail") -> jsonObject.getString("detail")
                jsonObject.has("message") -> jsonObject.getString("message")
                else -> "Error code: $defaultCode"
            }
        } catch (e: Exception) {
            "Error code: $defaultCode"
        }
    }
}
