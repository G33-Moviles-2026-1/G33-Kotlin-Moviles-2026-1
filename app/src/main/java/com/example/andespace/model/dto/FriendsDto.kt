package com.example.andespace.model.dto

data class FriendItemOut(
    val email: String,
    val username: String
)

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