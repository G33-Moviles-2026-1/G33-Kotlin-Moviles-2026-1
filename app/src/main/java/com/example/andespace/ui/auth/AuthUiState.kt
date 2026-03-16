package com.example.andespace.ui.auth

data class AuthUiState (
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)