package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName

data class FriendItemOut(
    val email: String,
    val username: String,
    val status: String? = null
) {
    val activityStatus: UserStatus
        get() = UserStatus.fromValue(status ?: UserStatus.FREE.value)

    val displayName: String
        get() {
            val localPart = username.substringBefore("@").ifEmpty { username }
            return localPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

data class MyFriendsResponse(
    val total: Int,
    val items: List<FriendItemOut>
)

data class CreateFriendshipRequest(
    val correo_amigo_2: String
)

data class AcceptFriendshipRequest(
    val correo_amigo_1: String
)

data class FriendshipSuggestion(
    val username: String
)
