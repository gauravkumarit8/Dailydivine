package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user, always id=1
    val name: String,
    val religionId: Int,
    val languageCode: String,
    val themeMode: String,   // "light", "dark", "system"
    val fontSize: String,    // "small", "medium", "large", "xlarge"
    val isPremium: Boolean = false,
    val premiumPurchaseDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
