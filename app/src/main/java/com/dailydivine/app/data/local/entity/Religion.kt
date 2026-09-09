package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "religions")
data class Religion(
    @PrimaryKey val id: Int,
    val name: String,                 // "Hinduism"
    val icon: String,                 // drawable resource name
    val colorPrimary: String,         // "#FF5722"
    val colorSecondary: String,       // "#FFC107"
    val totalVerses: Int,
    val availableLanguages: String,   // JSON array: ["en","hi","sa"]
    val description: String
)
