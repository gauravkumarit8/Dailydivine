package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: Int,
    val religionId: Int,
    val name: String,          // "Bhagavad Gita"
    val icon: String,
    val description: String,
    val verseCount: Int,
    val sortOrder: Int,
    val isPremium: Boolean = false
)
