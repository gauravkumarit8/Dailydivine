package com.dailydivine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailydivine.app.data.local.entity.ContentVersion

// v1.1 (NEW) — supports the Content Migration Manager (F002-R13)
@Dao
interface ContentVersionDao {
    @Query("SELECT * FROM content_versions WHERE religionId = :religionId LIMIT 1")
    suspend fun get(religionId: Int): ContentVersion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(version: ContentVersion)
}
