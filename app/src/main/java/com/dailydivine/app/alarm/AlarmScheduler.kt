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
 * v1.1 / F004-R25: on Android 12+ (API 31+), SCHEDULE_EXACT_ALARM requires an
 * explicit user grant. If it hasn't been granted, this class falls back to
 * AlarmManager.setWindow() (inexact but always legal); the UI surfaces the
 * banner via [canScheduleExactAlarms].
 *
 * Request-code layout (IMPORTANT): the daily alarm uses request code = alarm.id,
 * snoozes use SNOOZE_REQUEST_OFFSET + alarm.id. They MUST differ: PendingIntent
 * identity is what AlarmManager keys on, so sharing one code made a snooze
 * silently overwrite tomorrow's daily alarm.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /**
     * Schedules the next occurrence of [alarm].
     * @param afterFiring true when called from AlarmReceiver right after the
     *   alarm rang: the next trigger must then be at least [AFTER_FIRING_MIN_LEAD_MS]
     *   in the future, so an alarm delivered a moment early (inexact fallback)
     *   can't re-arm itself for "now" and ring twice.
     */
    fun schedule(alarm: Alarm, afterFiring: Boolean = false) {
        val pendingIntent = buildPendingIntent(alarm, requestCode = alarm.id, isSnooze = false, cyclesUsed = 0)
        val minLead = if (afterFiring) AFTER_FIRING_MIN_LEAD_MS else 0L
        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays, minLead)

        if (canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, INEXACT_WINDOW_MS, pendingIntent)
        }
    }

    /** Cancels both the daily alarm and any pending snooze for [alarmId]. */
    fun cancel(alarmId: Int) {
        listOf(alarmId, SNOOZE_REQUEST_OFFSET + alarmId).forEach { code ->
            val pi = PendingIntent.getBroadcast(
                context, code, Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it); it.cancel() }
        }
    }

    /**
     * Re-fire in [minutesFromNow] minutes (manual snooze and v1.1 auto-snooze).
     * [cyclesUsed] is carried in the Intent so AlarmService's 6-cycle cap
     * survives the service being destroyed between cycles.
     */
    fun scheduleSnooze(alarm: Alarm, minutesFromNow: Int, cyclesUsed: Int = 0) {
        val pendingIntent = buildPendingIntent(
            alarm, requestCode = SNOOZE_REQUEST_OFFSET + alarm.id, isSnooze = true, cyclesUsed = cyclesUsed
        )
        fireAt(System.currentTimeMillis() + minutesFromNow * 60_000L, pendingIntent)
    }

    /** Debug helper: rings the full alarm path (Receiver -> Service -> Ring screen)
     *  in [seconds] seconds, without touching any saved alarm. */
    fun scheduleTest(seconds: Int) {
        val testAlarm = Alarm(
            id = TEST_ALARM_ID, hour = 0, minute = 0, repeatDays = "[]",
            alarmToneId = "temple_bell", createdAt = 0L
        )
        val pendingIntent = buildPendingIntent(
            testAlarm, requestCode = SNOOZE_REQUEST_OFFSET + TEST_ALARM_ID, isSnooze = true, cyclesUsed = 0
        )
        fireAt(System.currentTimeMillis() + seconds * 1000L, pendingIntent)
    }

    private fun fireAt(triggerTime: Long, pendingIntent: PendingIntent) {
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, INEXACT_WINDOW_MS, pendingIntent)
        }
    }

    private fun buildPendingIntent(alarm: Alarm, requestCode: Int, isSnooze: Boolean, cyclesUsed: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_TONE, alarm.alarmToneId)
            putExtra(EXTRA_TTS_ENABLED, alarm.isTTSEnabled)
            putExtra(EXTRA_IS_SNOOZE, isSnooze)
            putExtra(EXTRA_SNOOZE_CYCLES, cyclesUsed)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun calculateNextTriggerTime(
        hour: Int, minute: Int, repeatDaysJson: String, minLeadMs: Long
    ): Long = computeNextTriggerTime(hour, minute, repeatDaysJson, minLeadMs)

    companion object {
        /**
         * Next trigger time strictly after now + [minLeadMs], on one of the days in
         * [repeatDaysJson] (ISO weekdays, 1 = Monday ... 7 = Sunday, e.g. "[1,2,3,4,5]").
         * An empty/unparseable list is treated as "every day". Pure (the clock is a
         * parameter) so it is unit-tested, and public so the UI can show "Rings in ...".
         */
        fun computeNextTriggerTime(
            hour: Int, minute: Int, repeatDaysJson: String,
            minLeadMs: Long = 0L, nowMs: Long = System.currentTimeMillis()
        ): Long {
            val days = parseRepeatDays(repeatDaysJson)
            val earliest = nowMs + minLeadMs
            val candidate = Calendar.getInstance().apply {
                timeInMillis = nowMs
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // At most 8 steps: today + a full week always contains a matching day.
            repeat(8) {
                val isoDay = (candidate.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // Calendar: Sun=1 -> ISO: Mon=1..Sun=7
                if (candidate.timeInMillis > earliest && isoDay in days) return candidate.timeInMillis
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            return candidate.timeInMillis
        }

        /** "[1,2,3]" -> {1,2,3}; empty or unparseable -> every day. */
        fun parseRepeatDays(json: String): Set<Int> {
            val parsed = Regex("\\d+").findAll(json).map { it.value.toInt() }.filter { it in 1..7 }.toSet()
            return if (parsed.isEmpty()) (1..7).toSet() else parsed
        }

        fun formatRepeatDays(days: Set<Int>): String = days.sorted().joinToString(",", "[", "]")

        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_TONE = "ALARM_TONE"
        const val EXTRA_TTS_ENABLED = "TTS_ENABLED"
        const val EXTRA_IS_SNOOZE = "IS_SNOOZE"
        const val EXTRA_SNOOZE_CYCLES = "SNOOZE_CYCLES"
        const val SNOOZE_REQUEST_OFFSET = 100_000
        const val TEST_ALARM_ID = 9999
        private const val INEXACT_WINDOW_MS = 10 * 60 * 1000L
        private const val AFTER_FIRING_MIN_LEAD_MS = 2 * 60 * 1000L
    }
}
