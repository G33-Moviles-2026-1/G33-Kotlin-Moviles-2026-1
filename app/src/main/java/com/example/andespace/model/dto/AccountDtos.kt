package com.example.andespace.model.dto

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

data class ChangeUsernameRequest(
    val username: String
)

data class MeProfileResponse(
    val email: String,
    val username: String,
    val status: String
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
            entries.firstOrNull { it.value == value } ?: INCOGNITO
    }
}
