package com.example.andespace.model.dto

import com.google.gson.annotations.SerializedName

data class AddFriendRequest(
    @SerializedName(value = "username", alternate = ["friend_username"]) val username: String
)

data class GetFriendsResponse(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("items") val items: List<FriendItemDto> = emptyList()
)

data class FriendItemDto(
    @SerializedName(value = "username", alternate = ["friend_username", "user_email"]) val username: String? = null,
    @SerializedName("status") val status: String? = null
) {
    fun toUiModel(): FriendUiModel? {
        val name = username?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return FriendUiModel(
            username = name,
            status = UserStatus.fromValue(status ?: UserStatus.FREE.value)
        )
    }
}

data class FriendUiModel(
    val username: String,
    val status: UserStatus
) {
    val displayName: String
        get() {
            val localPart = username.substringBefore("@").ifEmpty { username }
            return localPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
