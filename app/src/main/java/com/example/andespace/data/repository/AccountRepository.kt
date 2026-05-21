package com.example.andespace.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.dataStore
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.ChangeEmailRequest
import com.example.andespace.model.dto.ChangePasswordRequest
import com.example.andespace.model.dto.ChangeStatusRequest
import com.example.andespace.model.dto.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AccountRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    private val userEmailKey = stringPreferencesKey("account_user_email")
    private val userStatusKey = stringPreferencesKey("account_user_status")

    fun observeStatus(): Flow<UserStatus> = context.dataStore.data.map { prefs ->
        UserStatus.fromValue(prefs[userStatusKey] ?: UserStatus.FREE.value)
    }

    suspend fun loadCachedProfile(): Pair<String, UserStatus> = withContext(Dispatchers.IO) {
        val prefs = context.dataStore.data.first()
        val email = prefs[userEmailKey] ?: ""
        val status = UserStatus.fromValue(prefs[userStatusKey] ?: UserStatus.FREE.value)
        Pair(email, status)
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { prefs -> prefs[userEmailKey] = email }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.changePassword(
                    ChangePasswordRequest(current_password = currentPassword, new_password = newPassword)
                )
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
                }
            } catch (e: Exception) {
                Result.failure(Exception(RepositoryMessages.NO_INTERNET))
            }
        }

    suspend fun changeEmail(newEmail: String, currentPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.changeEmail(
                    ChangeEmailRequest(new_email = newEmail, current_password = currentPassword)
                )
                if (response.isSuccessful) {
                    context.dataStore.edit { prefs -> prefs[userEmailKey] = newEmail }
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val daysLeft = extractDaysLeft(errorBody)
                    if (daysLeft != null) {
                        Result.failure(CooldownException(daysLeft))
                    } else {
                        Result.failure(ApiException(extractErrorMessage(errorBody, response.code())))
                    }
                }
            } catch (e: Exception) {
                Result.failure(Exception(RepositoryMessages.NO_INTERNET))
            }
        }

    suspend fun changeStatus(status: UserStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.changeStatus(ChangeStatusRequest(status = status.value))
                if (response.isSuccessful) {
                    context.dataStore.edit { prefs -> prefs[userStatusKey] = status.value }
                    Result.success(Unit)
                } else {
                    Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
                }
            } catch (e: Exception) {
                Result.failure(Exception(RepositoryMessages.NO_INTERNET))
            }
        }

    private fun extractDaysLeft(errorBody: String?): Int? {
        if (errorBody.isNullOrEmpty()) return null
        return try {
            val json = JSONObject(errorBody)
            if (json.has("days_left")) json.getInt("days_left") else null
        } catch (_: Exception) {
            null
        }
    }
}

class CooldownException(val daysLeft: Int) : Exception("Podrás cambiar tu correo en $daysLeft días.")
