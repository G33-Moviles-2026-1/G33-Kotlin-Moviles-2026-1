package com.example.andespace.model.db.friends

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends_ui_draft")
data class FriendsUiDraftEntity(
    @PrimaryKey val userKey: String,
    val searchQuery: String
)
