package com.example.andespace.model.dto

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

data class ChangeEmailRequest(
    val new_email: String,
    val current_password: String
)

data class ChangeStatusRequest(
    val status: String
)

enum class UserStatus(val value: String) {
    INCOGNITO("incognito"),
    BUSY("busy"),
    EXERCISING("exercising"),
    FREE("free"),
    HANGING_OUT("hanging_out"),
    AT_HOME("at_home"),
    LUNCHING("lunching");

    companion object {
        fun fromValue(value: String): UserStatus =
            entries.firstOrNull { it.value == value } ?: FREE
    }
}
