package com.dailydivine.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.dailydivine.app.data.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F004-R03: fired by AlarmManager; starts the foreground AlarmService so
 * ringing survives even if the app process was killed.
 *
 * Also re-arms the NEXT occurrence: setAlarmClock/setWindow are one-shot, so
 * without this a daily alarm rang exactly once and never again.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        // Start ringing first: startForegroundService must happen promptly.
        val serviceIntent = Intent(context, AlarmService::class.java).apply { putExtras(intent) }
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            // Android 12+ can refuse a foreground-service start from the background (for
            // example when exact alarms were not allowed and the inexact fallback fired).
            // Never crash the receiver: it must still re-arm tomorrow. Raise a loud backup
            // notification so the user is woken anyway.
            Log.e("AlarmReceiver", "Could not start AlarmService, posting fallback alert", e)
            AlarmNotifications.postFallback(
                context,
                alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1),
                toneId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TONE) ?: "temple_bell",
                snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, AlarmScheduler.DEFAULT_SNOOZE_MINUTES)
            )
        }

        val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        if (isSnooze || alarmId < 0) return // snoozes/tests never re-arm the daily alarm

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmRepository.rescheduleNextOccurrence(alarmId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
