package com.dailydivine.app.alarm

import android.content.Intent

/**
 * Broadcasts after which every alarm and daily job must be armed again.
 *
 *  - BOOT_COMPLETED: AlarmManager forgets everything on reboot.
 *  - MY_PACKAGE_REPLACED: safety net after an app update.
 *  - TIMEZONE_CHANGED / TIME_SET: the alarms are stored as absolute instants, so after
 *    travelling (or a manual clock change) a "7:00" alarm would ring at the wrong local time.
 *  - SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED: Android cancels all exact alarms when
 *    the permission is revoked, and alarms armed through the inexact fallback stay inexact
 *    after it is granted again, until something re-arms them.
 *
 * Pure (no Android calls) so it can be unit-tested.
 */
object RearmTriggers {

    /** Same string as AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED (API 31). */
    const val ACTION_EXACT_ALARM_PERMISSION_CHANGED =
        "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"

    val ACTIONS: Set<String> = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_TIME_CHANGED,
        ACTION_EXACT_ALARM_PERMISSION_CHANGED
    )

    fun isRearmAction(action: String?): Boolean = action != null && action in ACTIONS
}
