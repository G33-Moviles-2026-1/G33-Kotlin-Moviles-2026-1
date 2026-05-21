package com.example.andespace.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.andespace.data.network.ApiService
import com.example.andespace.data.network.dataStore
import com.example.andespace.data.repository.shared.ApiException
import com.example.andespace.data.repository.shared.RepositoryMessages
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.ChangePasswordRequest
import com.example.andespace.model.dto.ChangeStatusRequest
import com.example.andespace.model.dto.ChangeUsernameRequest
import com.example.andespace.model.dto.MeProfileResponse
import com.example.andespace.model.dto.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class CachedUserProfile(
    val email: String,
    val username: String,
    val status: UserStatus
)

class AccountRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    private val userEmailKey = stringPreferencesKey("account_user_email")
    private val userUsernameKey = stringPreferencesKey("account_user_username")
    private val userStatusKey = stringPreferencesKey("account_user_status")

    fun observeStatus(): Flow<UserStatus> = context.dataStore.data.map { prefs ->
        UserStatus.fromValue(prefs[userStatusKey] ?: UserStatus.INCOGNITO.value)
    }

    suspend fun loadCachedProfile(): CachedUserProfile = withContext(Dispatchers.IO) {
        val prefs = context.dataStore.data.first()
        CachedUserProfile(
            email = prefs[userEmailKey] ?: "",
            username = prefs[userUsernameKey] ?: "",
            status = UserStatus.fromValue(prefs[userStatusKey] ?: UserStatus.INCOGNITO.value)
        )
    }

    suspend fun saveStatusLocally(status: UserStatus) = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs -> prefs[userStatusKey] = status.value }
    }

    suspend fun fetchProfile(): Result<MeProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMeProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    saveProfileLocally(body)
                    Result.success(body)
                } else {
                    Result.failure(ApiException(extractErrorMessage(null, response.code())))
                }
            } else {
                Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
            }
        } catch (_: Exception) {
            Result.failure(Exception(RepositoryMessages.NO_INTERNET))
        }
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
            } catch (_: Exception) {
                Result.failure(Exception(RepositoryMessages.NO_INTERNET))
            }
        }

    suspend fun changeUsername(newUsername: String): Result<MeProfileResponse> =
        withContext(Dispatchers.IO) {
            try {
                val normalized = newUsername.trim().lowercase()
                val response = apiService.changeUsername(ChangeUsernameRequest(username = normalized))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        saveProfileLocally(body)
                        Result.success(body)
                    } else {
                        Result.failure(ApiException(extractErrorMessage(null, response.code())))
                    }
                } else {
                    Result.failure(ApiException(extractErrorMessage(response.errorBody()?.string(), response.code())))
                }
            } catch (_: Exception) {
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
            } catch (_: Exception) {
                Result.failure(Exception(RepositoryMessages.NO_INTERNET))
            }
        }

    private suspend fun saveProfileLocally(profile: MeProfileResponse) {
        context.dataStore.edit { prefs ->
            prefs[userEmailKey] = profile.email
            prefs[userUsernameKey] = profile.username
            prefs[userStatusKey] = profile.status
        }
    }
}
