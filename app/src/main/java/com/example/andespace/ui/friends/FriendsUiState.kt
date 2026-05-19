package com.example.andespace.ui.friends

import com.example.andespace.model.dto.FriendUiModel
import com.example.andespace.model.dto.UserStatus

data class FriendsUiState(
    val friends: List<FriendUiModel> = emptyList(),
    val myStatus: UserStatus = UserStatus.FREE,
    val isLoading: Boolean = false,
    val isAddingFriend: Boolean = false,
    val showAddFriendDialog: Boolean = false,
    val showStatusPicker: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
