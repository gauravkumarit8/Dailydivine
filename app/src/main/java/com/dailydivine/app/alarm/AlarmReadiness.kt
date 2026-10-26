package com.dailydivine.app.alarm

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/**
 * Can the ringing alarm actually show its screen and be stopped? Used for the banners on the
 * Alarm tab. Without these, a user who muted notifications (Android 13+) or denied full-screen
 * intents (Android 14+) would hear a ringing alarm with nothing to tap.
 */
object AlarmReadiness {

    /** Notifications on for the app, and the ringing channel not turned down below "high". */
    fun notificationsReady(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return true
            val importance = nm.getNotificationChannel(AlarmNotifications.CHANNEL_RING)?.importance
                ?: return true // channel not created yet: nothing is blocked
            if (importance < NotificationManager.IMPORTANCE_HIGH) return false
        }
        return true
    }

    /** Android 14+ lets the user revoke full-screen intents; earlier versions always allow. */
    fun fullScreenIntentReady(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
}
