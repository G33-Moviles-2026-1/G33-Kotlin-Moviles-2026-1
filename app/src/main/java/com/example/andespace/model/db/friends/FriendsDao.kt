package com.example.andespace.model.db.friends

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FriendsDao {
    @Query("SELECT * FROM cached_friends WHERE userKey = :userKey")
    suspend fun getAllForUser(userKey: String): List<CachedFriendEntity>

    @Query("SELECT * FROM cached_friends WHERE userKey = :userKey AND role = :role")
    suspend fun getFriendsByRole(userKey: String, role: String): List<CachedFriendEntity>

    @Query("DELETE FROM cached_friends WHERE userKey = :userKey AND role = :role")
    suspend fun clearByRole(userKey: String, role: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<CachedFriendEntity>)

    @Query("DELETE FROM cached_friends WHERE userKey = :userKey AND email = :email")
    suspend fun deleteByEmail(userKey: String, email: String)

    @Query("SELECT searchQuery FROM friends_ui_draft WHERE userKey = :userKey LIMIT 1")
    suspend fun getSearchDraft(userKey: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchDraft(draft: FriendsUiDraftEntity)
}
