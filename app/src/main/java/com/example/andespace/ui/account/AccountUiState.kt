package com.example.andespace.ui.account

data class AccountUiState(
    val currentEmail: String = "",
    val currentUsername: String = "",
    val isLoadingProfile: Boolean = false,
    val isLoadingPasswordChange: Boolean = false,
    val isLoadingUsernameChange: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)
