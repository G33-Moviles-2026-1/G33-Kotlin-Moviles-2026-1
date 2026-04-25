package com.example.andespace.data.repository.shared

import com.example.andespace.data.network.ApiService
import org.json.JSONObject

object RepositoryMessages {
    const val GENERIC_ERROR = "Something went wrong. Please try again."
    const val NO_INTERNET = "No internet connection. Please check your network and try again."
    const val DELETE_BOOKING_FAILED = "Could not delete the booking. Please try again."
    const val SAVE_BOOKING_FAILED = "Could not save the booking. Please try again."
    const val FAVORITES_SYNC_FAILED = "Couldn't sync favorites right now. Showing local favorites."
}

fun extractErrorMessage(errorBody: String?, defaultCode: Int): String {
    if (!errorBody.isNullOrEmpty()) {
        return try {
            val jsonObject = JSONObject(errorBody)
            when {
                jsonObject.has("detail") -> jsonObject.getString("detail")
                jsonObject.has("message") -> jsonObject.getString("message")
                else -> httpErrorMessage(defaultCode)
            }
        } catch (_: Exception) {
            httpErrorMessage(defaultCode)
        }
    }
    return httpErrorMessage(defaultCode)
}

fun httpErrorMessage(code: Int): String = when (code) {
    400 -> "The request was not valid. Please check your input."
    401 -> "Your session has expired. Please log in again."
    403 -> "You don't have permission to perform this action."
    404 -> "The requested information was not found."
    408, 504 -> "The request took too long. Please try again."
    409 -> "There was a conflict with your request. Please try again."
    422 -> "The submitted information is not valid."
    429 -> "Too many requests. Please wait a moment and try again."
    500, 502, 503 -> "The server is temporarily unavailable. Please try again later."
    else -> RepositoryMessages.GENERIC_ERROR
}

class ApiException(message: String) : Exception(message) {
    override val message: String
        get() = super.message?.removePrefix("java.lang.Exception: ")?.trim()
            ?: RepositoryMessages.GENERIC_ERROR
}

class ScheduleValidator(
    private val apiService: ApiService
) {
    suspend fun checkIfScheduleExists(): Result<Boolean> {
        return try {
            val response = apiService.getScheduleClasses()
            if (response.isSuccessful) {
                val bodyString = response.body().toString()
                val hasSchedule = !bodyString.contains("\"classes\": []") && bodyString.length > 10
                Result.success(hasSchedule)
            } else {
                val backendMessage = extractErrorMessage(response.errorBody()?.string(), response.code())
                Result.failure(ApiException(backendMessage))
            }
        } catch (_: Exception) {
            Result.failure(ApiException(RepositoryMessages.NO_INTERNET))
        }
    }
}
