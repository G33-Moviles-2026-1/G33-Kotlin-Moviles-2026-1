package com.example.andespace.model.db.favorites

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRoomEntity): Long

    @Query("SELECT * FROM favorite_rooms WHERE userKey = :userKey ORDER BY roomId ASC")
    suspend fun getFavoritesByUser(userKey: String): List<FavoriteRoomEntity>

    @Query("DELETE FROM favorite_rooms WHERE userKey = :userKey AND roomId = :roomId")
    suspend fun deleteFavoriteByRoomId(userKey: String, roomId: String): Int

    @Query("DELETE FROM favorite_rooms WHERE userKey = :userKey")
    suspend fun clearFavoritesForUser(userKey: String): Int
}