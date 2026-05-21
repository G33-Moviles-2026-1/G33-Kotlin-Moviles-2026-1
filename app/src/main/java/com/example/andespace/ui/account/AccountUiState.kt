package com.example.andespace.ui.account

import com.example.andespace.model.dto.UserStatus

data class AccountUiState(
    val currentEmail: String = "",
    val currentStatus: UserStatus = UserStatus.FREE,
    val isLoadingPasswordChange: Boolean = false,
    val isLoadingEmailChange: Boolean = false,
    val isLoadingStatusChange: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val emailCooldownDaysLeft: Int? = null,
    val isOffline: Boolean = false
)
