package com.dailydivine.app

import android.app.Application
import com.dailydivine.app.alarm.AlarmNotifications
import com.dailydivine.app.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailyDivineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        AlarmNotifications.ensureChannels(this)
    }
}
