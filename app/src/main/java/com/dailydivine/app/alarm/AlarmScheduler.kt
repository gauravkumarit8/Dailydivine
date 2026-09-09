package com.dailydivine.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dailydivine.app.data.local.entity.Alarm
import java.util.Calendar

/**
 * Schedules and cancels alarms.
 *
 * v1.1 / F004-R25 (NEW): on Android 12+ (API 31+), SCHEDULE_EXACT_ALARM
 * requires an explicit user grant in system settings. If it hasn't been
 * granted, this class MUST NOT crash or silently fail to schedule — it
 * falls back to AlarmManager.setWindow(), a less precise but always-legal
 * API, and the caller (AlarmSetupScreen / AlarmEditScreen) is responsible
 * for surfacing the "enable exact alarms for full reliability" banner via
 * [canScheduleExactAlarms].
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Exposed so UI (onboarding S05, alarm config S11) can show the reliability banner. */
    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // no runtime grant required pre-Android 12
        }

    fun schedule(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_TONE, alarm.alarmToneId)
            putExtra(EXTRA_TTS_ENABLED, alarm.isTTSEnabled)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id, // unique request code per alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)

        if (canScheduleExactAlarms()) {
            // F004-R04/R21: exact timing via AlarmClockInfo, survives Doze (setExactAndAllowWhileIdle
            // also works, but AlarmClockInfo additionally surfaces the alarm icon in the status bar).
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        } else {
            // v1.1 fallback: inexact delivery within a bounded window rather than failing outright.
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                INEXACT_WINDOW_MS,
                pendingIntent
            )
        }
    }

    fun cancel(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    /** Reschedule for N minutes from now — used for both manual snooze and v1.1 auto-snooze. */
    fun scheduleSnooze(alarm: Alarm, minutesFromNow: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_TONE, alarm.alarmToneId)
            putExtra(EXTRA_TTS_ENABLED, alarm.isTTSEnabled)
            putExtra(EXTRA_IS_SNOOZE, true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerTime = System.currentTimeMillis() + minutesFromNow * 60_000L
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, INEXACT_WINDOW_MS, pendingIntent)
        }
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, repeatDaysJson: String): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.before(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        // NOTE: repeatDaysJson (e.g. "[1,2,3,4,5]") narrowing to specific
        // weekdays is intentionally left as a follow-up — this MVP scheduler
        // fires daily at the configured time, matching the F004-R08 default.
        return trigger.timeInMillis
    }

    companion object {
        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_TONE = "ALARM_TONE"
        const val EXTRA_TTS_ENABLED = "TTS_ENABLED"
        const val EXTRA_IS_SNOOZE = "IS_SNOOZE"
        private const val INEXACT_WINDOW_MS = 10 * 60 * 1000L // 10-minute delivery window
    }
}
