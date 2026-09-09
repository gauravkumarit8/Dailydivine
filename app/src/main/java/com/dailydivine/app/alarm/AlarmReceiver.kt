package com.dailydivine.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** F004-R03: fired by AlarmManager; starts the foreground AlarmService so
 *  ringing survives even if the app process was killed. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtras(intent)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
