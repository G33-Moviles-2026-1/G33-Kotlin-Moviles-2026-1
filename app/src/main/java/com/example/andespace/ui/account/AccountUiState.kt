package com.example.andespace.ui.account

import com.example.andespace.model.dto.UserStatus

data class AccountUiState(
    val currentEmail: String = "",
    val currentUsername: String = "",
    val currentStatus: UserStatus = UserStatus.INCOGNITO,
    val isLoadingProfile: Boolean = false,
    val isLoadingPasswordChange: Boolean = false,
    val isLoadingUsernameChange: Boolean = false,
    val isLoadingStatusChange: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)
