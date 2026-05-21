package com.example.andespace.model.db.friends

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_friends",
    indices = [Index(value = ["userKey", "email", "role"], unique = true)]
)
data class CachedFriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userKey: String,
    val email: String,
    val username: String,
    val status: String,
    val role: String
)
