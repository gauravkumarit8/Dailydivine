package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "verses",
    indices = [
        Index(value = ["religionId", "dayNumber"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["religionId"])
    ]
)
data class Verse(
    @PrimaryKey val id: Int,
    val religionId: Int,
    val categoryId: Int,
    val dayNumber: Int,             // 1-730 for daily assignment (F002-R04)
    val originalText: String,
    val translatedText: String,
    val sourceReference: String,    // "Bhagavad Gita 2.20"
    val languageCode: String,
    val audioFilePath: String? = null,
    val isPremium: Boolean = false,
    val tags: String? = null        // comma-separated, used for search (F006-R04/05)
)
