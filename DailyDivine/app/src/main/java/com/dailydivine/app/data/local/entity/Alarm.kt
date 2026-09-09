package com.dailydivine.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,                  // 0-23
    val minute: Int,                // 0-59
    val isEnabled: Boolean = true,
    val repeatDays: String,         // JSON: [1,2,3,4,5] (Mon-Fri)
    val alarmToneId: String,
    val volume: Int = 80,           // 0-100
    val isGradualVolume: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isTTSEnabled: Boolean = false,
    val playAudioAfter: Boolean = false,
    val snoozeDurationMinutes: Int = 10,
    val label: String = "Morning Devotion",
    val createdAt: Long
    // Note (v1.1): the 30-min escalation window and 6-cycle auto-snooze cap
    // from F004-R19 are AlarmService/AlarmEscalationController runtime state,
    // not persisted per-alarm fields — see alarm/AlarmEscalationController.kt.
)
