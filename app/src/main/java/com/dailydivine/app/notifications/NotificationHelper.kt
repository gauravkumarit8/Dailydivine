package com.dailydivine.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dailydivine.app.ui.MainActivity

/**
 * Builds and posts the F009 notifications. Every post is skipped (never
 * crashes) when the user hasn't granted notification permission or has
 * disabled notifications for the app. Tapping any of them opens the app,
 * whose Home screen shows the daily verse and records the day as opened.
 */
object NotificationHelper {

    private const val ID_DAILY_VERSE = 2001
    private const val ID_STREAK_REMINDER = 2002
    private const val ID_MILESTONE = 2003

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** F009-R03: first 100 characters, line breaks flattened. */
    fun preview(text: String): String {
        val flat = text.replace(Regex("\\s*\\n\\s*"), " ").trim()
        return if (flat.length > 100) flat.take(100).trimEnd() + "\u2026" else flat
    }

    fun postDailyVerse(context: Context, verseText: String, reference: String) {
        val openApp = openAppIntent(context, 0)
        post(
            context, ID_DAILY_VERSE,
            NotificationCompat.Builder(context, NotificationChannels.DAILY_VERSE)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Today's verse")
                .setContentText(preview(verseText))
                .setStyle(NotificationCompat.BigTextStyle().bigText(preview(verseText) + "\n\u2014 " + reference))
                .setContentIntent(openApp)
                .addAction(0, "Read", openApp) // F009-R05
                .setAutoCancel(true)
                .build()
        )
    }

    fun postStreakReminder(context: Context, streakDays: Int) {
        post(
            context, ID_STREAK_REMINDER,
            NotificationCompat.Builder(context, NotificationChannels.STREAK_REMINDER)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Don't break your $streakDays-day streak!") // F009-R07
                .setContentText("Open DailyDivine to read today's verse.")
                .setContentIntent(openAppIntent(context, 1))
                .setAutoCancel(true)
                .build()
        )
    }

    fun postMilestone(context: Context, days: Int, badgeName: String) {
        val dayWord = if (days == 1) "1 day" else "$days days"
        post(
            context, ID_MILESTONE,
            NotificationCompat.Builder(context, NotificationChannels.MILESTONES)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Congratulations!") // F009-R11
                .setContentText("You've reached $dayWord! \u2014 $badgeName")
                .setContentIntent(openAppIntent(context, 2, openBadges = true)) // F009-R12
                .setAutoCancel(true)
                .build()
        )
    }

    private fun openAppIntent(context: Context, requestCode: Int, openBadges: Boolean = false): PendingIntent =
        PendingIntent.getActivity(
            context, requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (openBadges) putExtra(MainActivity.EXTRA_OPEN_BADGES, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    @SuppressLint("MissingPermission") // guarded by canPost()
    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        if (!canPost(context)) return
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check and the post; nothing to do.
        }
    }
}
