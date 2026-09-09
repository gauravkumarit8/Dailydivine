package com.dailydivine.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** F004-R05/R22: alarms must survive a device reboot. Re-reads every
 *  enabled alarm from Room and re-schedules it via AlarmScheduler. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // TODO(Sprint 4 wiring): replace with Hilt-injected AlarmRepository
        // once di/DatabaseModule provides AppDatabase to non-Compose entry
        // points. Sketch:
        //
        // CoroutineScope(Dispatchers.IO).launch {
        //     val db = ... get AppDatabase instance ...
        //     val alarms = db.alarmDao().getEnabledAlarms()
        //     alarms.forEach { alarm -> AlarmScheduler(context).schedule(alarm) }
        // }
    }
}
