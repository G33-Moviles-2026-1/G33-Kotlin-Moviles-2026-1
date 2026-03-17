package com.example.andespace.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel(){
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onRepeatPasswordChange(repPassword: String) {
        _uiState.update { it.copy(repeatPassword = repPassword) }
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.login(uiState.value.email, uiState.value.password)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, password = "", email = "", repeatPassword = "") }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown authentication error"
                    )
                }
            }
        }
    }
    fun onRegisterClick(onSuccess: () -> Unit) {
        if (uiState.value.password != uiState.value.repeatPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.register(uiState.value.email, uiState.value.password, "2026-10")

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, password = "", email = "", repeatPassword = "") }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown registration error"
                    )
                }
            }
        }
    }
}