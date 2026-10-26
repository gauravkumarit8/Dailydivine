package com.dailydivine.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailydivine.app.data.local.datastore.UserPreferences
import com.dailydivine.app.data.repository.AlarmRepository
import com.dailydivine.app.jobs.DailyAlarmScheduler
import com.dailydivine.app.jobs.plansFor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F004-R05/R22: alarms must survive a device reboot. Re-reads every
 * enabled alarm from Room and re-schedules it via AlarmRepository.
 *
 * @AndroidEntryPoint lets Hilt inject into a BroadcastReceiver (not part of
 * the normal Activity/Fragment Hilt graph by default) -- this is the
 * standard, documented pattern for exactly this case.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmRepository: AlarmRepository
    @Inject lateinit var userPreferences: UserPreferences

    override fun onReceive(context: Context, intent: Intent) {
        // Reboot, app update, time-zone / clock change, exact-alarm permission change:
        // see RearmTriggers. Re-scheduling is idempotent, so extra triggers are harmless.
        if (!RearmTriggers.isRearmAction(intent.action)) return

        // goAsync() keeps the receiver (and its process) alive long enough
        // for the coroutine below to finish, since a plain onReceive()
        // return would let the system kill the process mid-reschedule.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmRepository.rescheduleAllEnabled()
                // The daily wallpaper / verse / streak alarms are forgotten on reboot too.
                DailyAlarmScheduler(context).sync(plansFor(userPreferences.state.first()), replace = true)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
