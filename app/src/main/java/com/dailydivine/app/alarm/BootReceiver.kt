package com.dailydivine.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailydivine.app.data.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // goAsync() keeps the receiver (and its process) alive long enough
        // for the coroutine below to finish, since a plain onReceive()
        // return would let the system kill the process mid-reschedule.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmRepository.rescheduleAllEnabled()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
