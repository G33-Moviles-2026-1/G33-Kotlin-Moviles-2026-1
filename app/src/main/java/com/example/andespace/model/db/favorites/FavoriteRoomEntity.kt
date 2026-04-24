package com.example.andespace.model.db.favorites

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_rooms",
    indices = [Index(value = ["userKey", "roomId"], unique = true)]
)
data class FavoriteRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userKey: String,
    val roomId: String,
    val name: String?,
    val building: String?,
    val buildingCode: String?,
    val capacity: Int?,
    val utilitiesJson: String
)