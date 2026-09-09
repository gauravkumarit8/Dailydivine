package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks", indices = [Index(value = ["verseId"], unique = true)])
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verseId: Int,
    val notes: String? = null,
    val createdAt: Long
)
