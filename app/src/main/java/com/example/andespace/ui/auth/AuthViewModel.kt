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

    fun onLoginClick(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.login(uiState.value.email, uiState.value.password)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    fun onRegisterClick(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.login(uiState.value.email, uiState.value.password)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}