package com.dailydivine.app.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dailydivine.app.ui.alarm.AlarmRingActivity

/**
 * The notification that goes with a ringing alarm.
 *
 * Two channels:
 *  - [CHANNEL_RING]: used while AlarmService is running. SILENT on purpose: the sound is the
 *    alarm tone played by the service itself. (The old "alarm_channel" had the default
 *    notification sound, which played on top of the tone.)
 *  - [CHANNEL_FALLBACK]: used only when the service could not be started (see AlarmReceiver);
 *    it makes its own alarm-stream sound so the user is still woken.
 *
 * The notification carries Snooze and Dismiss actions, so the alarm can be stopped even when
 * the full-screen ring screen is not shown (notifications muted, full-screen intent denied).
 */
object AlarmNotifications {

    const val CHANNEL_RING = "alarm_ring"
    const val CHANNEL_FALLBACK = "alarm_fallback"
    private const val LEGACY_CHANNEL = "alarm_channel"
    const val NOTIFICATION_ID = 4004

    private const val SNOOZE_ACTION_OFFSET = 300_000
    private const val DISMISS_ACTION_OFFSET = 400_000
    private const val FALLBACK_TIMEOUT_MS = 30 * 60 * 1000L
    private const val TAG = "AlarmNotifications"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.deleteNotificationChannel(LEGACY_CHANNEL)

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_RING, "Alarm (while ringing)", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Shown while your alarm is ringing, with Snooze and Dismiss. Keep this on so the alarm screen can open."
                setSound(null, null)
                enableVibration(false)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_FALLBACK, "Alarm (backup)", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Backup alert if the alarm could not start normally."
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
            }
        )
    }

    fun build(
        context: Context,
        alarmId: Int,
        toneId: String,
        settings: RingSettings,
        channelId: String = CHANNEL_RING
    ): Notification {
        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TONE, toneId)
            settings.writeTo(this) // the ring screen shows "Snooze - X min"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreen = PendingIntent.getActivity(
            context, alarmId, ringIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snooze = PendingIntent.getService(
            context, SNOOZE_ACTION_OFFSET + alarmId,
            actionIntent(context, AlarmService.ACTION_SNOOZE, alarmId, toneId, settings),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismiss = PendingIntent.getService(
            context, DISMISS_ACTION_OFFSET + alarmId,
            actionIntent(context, AlarmService.ACTION_DISMISS, alarmId, toneId, settings),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Good morning")
            .setContentText("Time for your morning blessing")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreen, true)
            .setContentIntent(fullScreen)
            .addAction(0, "Snooze ${settings.snoozeMinutes} min", snooze)
            .addAction(0, "Dismiss", dismiss)
            .setOngoing(true)
        if (channelId == CHANNEL_FALLBACK) builder.setTimeoutAfter(FALLBACK_TIMEOUT_MS)
        return builder.build()
    }

    /** Backup alert used when AlarmService could not be started from the background. */
    @SuppressLint("MissingPermission")
    fun postFallback(context: Context, alarmId: Int, toneId: String, settings: RingSettings) {
        try {
            ensureChannels(context)
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID,
                build(context, alarmId, toneId, settings, CHANNEL_FALLBACK)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fallback alarm notification failed", e)
        }
    }

    fun clear(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun actionIntent(
        context: Context, action: String, alarmId: Int, toneId: String, settings: RingSettings
    ) = Intent(context, AlarmService::class.java).apply {
        this.action = action
        putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        putExtra(AlarmScheduler.EXTRA_ALARM_TONE, toneId)
        settings.writeTo(this)
    }
}
