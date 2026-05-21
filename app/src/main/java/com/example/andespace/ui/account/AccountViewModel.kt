package com.example.andespace.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.network.NetworkMonitor
import com.example.andespace.data.repository.AccountRepository
import com.example.andespace.data.repository.CooldownException
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
        loadCachedProfile()
        observeNetwork()
    }

    private fun loadCachedProfile() {
        viewModelScope.launch {
            val (email, status) = repository.loadCachedProfile()
            _uiState.update { it.copy(currentEmail = email, currentStatus = status) }
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
            val result = repository.changePassword(currentPassword, newPassword)
            result.onSuccess {
                _uiState.update { it.copy(isLoadingPasswordChange = false, successMessage = "Password updated successfully.") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingPasswordChange = false, errorMessage = error.message ?: "Could not change password.") }
            }
        }
    }

    fun changeEmail(newEmail: String, currentPassword: String) {
        if (!newEmail.trim().lowercase().endsWith("@uniandes.edu.co")) {
            _uiState.update { it.copy(errorMessage = "Email must end in @uniandes.edu.co.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEmailChange = true, errorMessage = null, successMessage = null, emailCooldownDaysLeft = null) }
            val result = repository.changeEmail(newEmail.trim().lowercase(), currentPassword)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoadingEmailChange = false,
                        currentEmail = newEmail.trim().lowercase(),
                        successMessage = "Email updated successfully."
                    )
                }
            }.onFailure { error ->
                if (error is CooldownException) {
                    _uiState.update { it.copy(isLoadingEmailChange = false, emailCooldownDaysLeft = error.daysLeft) }
                } else {
                    _uiState.update { it.copy(isLoadingEmailChange = false, errorMessage = error.message ?: "Could not change email.") }
                }
            }
        }
    }

    fun changeStatus(status: UserStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStatusChange = true, errorMessage = null, successMessage = null) }
            val result = repository.changeStatus(status)
            result.onSuccess {
                _uiState.update { it.copy(isLoadingStatusChange = false, currentStatus = status, successMessage = "Status updated.") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingStatusChange = false, errorMessage = error.message ?: "Could not change status.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
