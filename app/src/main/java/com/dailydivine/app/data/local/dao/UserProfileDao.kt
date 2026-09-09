package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dailydivine.app.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfile?
}
