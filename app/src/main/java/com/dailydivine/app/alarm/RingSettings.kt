package com.dailydivine.app.alarm

import android.content.Intent
import com.dailydivine.app.data.local.entity.Alarm

/**
 * The per-alarm "how should it ring" settings, carried in the Intents that start the ring
 * (alarm, snooze, notification actions). AlarmService is destroyed between snooze cycles and
 * has no database access, so the settings travel with the Intent; without this every snooze
 * silently reverted to the defaults.
 */
data class RingSettings(
    val volumePercent: Int = DEFAULT_VOLUME,
    val gradual: Boolean = true,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = AlarmScheduler.DEFAULT_SNOOZE_MINUTES
) {
    fun writeTo(intent: Intent): Intent = intent.apply {
        putExtra(AlarmScheduler.EXTRA_VOLUME, volumePercent)
        putExtra(AlarmScheduler.EXTRA_GRADUAL, gradual)
        putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
        putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
    }

    /** A stand-in Alarm for rescheduling a snooze; only the ring settings and ids matter. */
    fun toPlaceholderAlarm(id: Int, toneId: String): Alarm = Alarm(
        id = id, hour = 0, minute = 0, repeatDays = "[]", alarmToneId = toneId,
        volume = volumePercent, isGradualVolume = gradual, isVibrationEnabled = vibrate,
        snoozeDurationMinutes = snoozeMinutes, createdAt = 0L
    )

    companion object {
        const val DEFAULT_VOLUME = 80
        val SNOOZE_CHOICES = listOf(5, 10, 15) // F004-R12

        fun fromAlarm(alarm: Alarm) = RingSettings(
            volumePercent = AlarmVolumeCurve.clampPercent(alarm.volume),
            gradual = alarm.isGradualVolume,
            vibrate = alarm.isVibrationEnabled,
            snoozeMinutes = alarm.snoozeDurationMinutes.coerceIn(1, 60)
        )

        fun fromIntent(intent: Intent?) = RingSettings(
            volumePercent = AlarmVolumeCurve.clampPercent(
                intent?.getIntExtra(AlarmScheduler.EXTRA_VOLUME, DEFAULT_VOLUME) ?: DEFAULT_VOLUME
            ),
            gradual = intent?.getBooleanExtra(AlarmScheduler.EXTRA_GRADUAL, true) ?: true,
            vibrate = intent?.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true) ?: true,
            snoozeMinutes = (intent?.getIntExtra(
                AlarmScheduler.EXTRA_SNOOZE_MINUTES, AlarmScheduler.DEFAULT_SNOOZE_MINUTES
            ) ?: AlarmScheduler.DEFAULT_SNOOZE_MINUTES).coerceIn(1, 60)
        )
    }
}
