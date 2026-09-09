package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_tones")
data class AlarmTone(
    @PrimaryKey val id: String,     // "temple_bell"
    val name: String,               // "Temple Bell"
    val fileName: String,           // "temple_bell.mp3"
    val religionId: Int? = null,    // null = universal
    val durationSeconds: Int,
    val isPremium: Boolean = false
)
