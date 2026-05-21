package com.example.andespace.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.AccountRepository
import com.example.andespace.model.dto.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeNetwork()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val cached = repository.loadCachedProfile()
            _uiState.update {
                it.copy(
                    currentEmail = cached.email,
                    currentUsername = cached.username,
                    currentStatus = cached.status
                )
            }
            _uiState.update { it.copy(isLoadingProfile = true, errorMessage = null) }
            repository.fetchProfile().fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoadingProfile = false,
                            currentEmail = profile.email,
                            currentUsername = profile.username,
                            currentStatus = UserStatus.fromValue(profile.status)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoadingProfile = false, errorMessage = error.message) }
                }
            )
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword.length < 8) {
            _uiState.update { it.copy(errorMessage = "New password must be at least 8 characters.") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPasswordChange = true, errorMessage = null, successMessage = null) }
            repository.changePassword(currentPassword, newPassword).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoadingPasswordChange = false, successMessage = "Password updated successfully.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoadingPasswordChange = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun changeUsername(newUsername: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsernameChange = true, errorMessage = null, successMessage = null) }
            repository.changeUsername(newUsername).fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoadingUsernameChange = false,
                            currentEmail = profile.email,
                            currentUsername = profile.username,
                            currentStatus = UserStatus.fromValue(profile.status),
                            successMessage = "Username updated successfully."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoadingUsernameChange = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun changeStatus(status: UserStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStatusChange = true, errorMessage = null, successMessage = null) }
            repository.changeStatus(status).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoadingStatusChange = false, currentStatus = status, successMessage = "Status updated.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoadingStatusChange = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
