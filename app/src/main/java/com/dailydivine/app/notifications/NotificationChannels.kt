package com.dailydivine.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * PRD F009 channels: Daily Verse (default), Streak Reminders (low),
 * Milestones (default). The Alarm (high) channel is created by AlarmService,
 * where it is used. Creating an existing channel is a no-op, so this is safe
 * to call on every launch.
 */
object NotificationChannels {
    const val DAILY_VERSE = "daily_verse"
    const val STREAK_REMINDER = "streak_reminder"
    const val MILESTONES = "milestones"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(DAILY_VERSE, "Daily verse", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Your verse for the day, every morning"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(STREAK_REMINDER, "Streak reminders", NotificationManager.IMPORTANCE_LOW).apply {
                description = "A gentle evening nudge to keep your streak alive"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(MILESTONES, "Milestones", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Celebrations when you reach a streak milestone"
            }
        )
    }
}
