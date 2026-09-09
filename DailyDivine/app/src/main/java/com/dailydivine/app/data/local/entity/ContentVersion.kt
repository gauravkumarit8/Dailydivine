package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * v1.1 (NEW) — backs F002-R13, the Content Migration Manager.
 * Tracks which JSON content "version" has already been applied per religion,
 * so app updates can diff-insert new verses without ever touching the
 * user's existing streaks, bookmarks, or verses.
 */
@Entity(tableName = "content_versions")
data class ContentVersion(
    @PrimaryKey val religionId: Int,
    val appliedVersion: Int,
    val lastMigratedAt: Long
)
