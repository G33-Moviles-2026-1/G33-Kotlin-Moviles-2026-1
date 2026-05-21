package com.example.andespace.data.repository

import com.example.andespace.model.dto.FriendItemOut
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.ui.friends.OutgoingFriendRequest

data class FriendsLocalSnapshot(
    val friends: List<FriendItemOut> = emptyList(),
    val incoming: List<FriendItemOut> = emptyList(),
    val outgoing: List<OutgoingFriendRequest> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val searchDraft: String = "",
    val myStatus: UserStatus = UserStatus.INCOGNITO
)

data class FriendsNetworkBundle(
    val friends: List<FriendItemOut>,
    val incoming: List<FriendItemOut>,
    /** null when the suggestions request failed; keeps the previous local cache. */
    val suggestions: List<String>? = null
)
