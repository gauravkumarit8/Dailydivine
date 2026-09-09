package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "streaks", indices = [Index(value = ["date"], unique = true)])
data class StreakEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,          // ISO format "2026-09-09"
    val verseId: Int,
    val wasRead: Boolean = true,
    val wasListened: Boolean = false,
    val journalText: String? = null,
    val moodEmoji: String? = null,
    val openedAt: Long
)
