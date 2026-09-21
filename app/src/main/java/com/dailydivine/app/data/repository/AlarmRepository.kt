package com.dailydivine.app.data.repository

import android.content.Context
import com.dailydivine.app.alarm.AlarmScheduler
import com.dailydivine.app.data.local.dao.AlarmDao
import com.dailydivine.app.data.local.entity.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Closes the gap where Alarm entities and AlarmScheduler existed since
 * Sprint 1 but nothing ever connected them to a real, persisted, rescheduled
 * alarm -- every write here both updates Room AND (re)schedules or cancels
 * the real system alarm, so the two can never drift out of sync.
 */
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    @ApplicationContext private val context: Context
) {
    private val scheduler by lazy { AlarmScheduler(context) }

    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun getEnabledAlarms(): List<Alarm> = alarmDao.getEnabledAlarms()

    suspend fun getAlarmById(id: Int): Alarm? = alarmDao.getAlarmById(id)

    /** Creates a new alarm, persists it, and schedules it in one step --
     *  used by onboarding (S05) to actually turn the user's alarm-setup
     *  choices into a real, firing alarm rather than just UI state. */
    suspend fun createAndSchedule(
        hour: Int,
        minute: Int,
        alarmToneId: String = "temple_bell",
        isTTSEnabled: Boolean = false,
        label: String = "Morning Devotion"
    ): Alarm {
        val alarm = Alarm(
            hour = hour,
            minute = minute,
            isEnabled = true,
            repeatDays = "[1,2,3,4,5,6,7]", // daily by default (F004-R08 narrowing is a follow-up)
            alarmToneId = alarmToneId,
            isTTSEnabled = isTTSEnabled,
            label = label,
            createdAt = System.currentTimeMillis()
        )
        val id = alarmDao.insertAlarm(alarm).toInt()
        val saved = alarm.copy(id = id)
        scheduler.schedule(saved)
        return saved
    }

    suspend fun updateAndReschedule(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
        if (alarm.isEnabled) {
            scheduler.schedule(alarm)
        } else {
            scheduler.cancel(alarm.id)
        }
    }

    suspend fun delete(alarm: Alarm) {
        scheduler.cancel(alarm.id)
        alarmDao.deleteAlarm(alarm)
    }

    /** F004-R22 (BootReceiver): re-arm every enabled alarm after reboot,
     *  since AlarmManager entries do not survive a device restart. */
    suspend fun rescheduleAllEnabled() {
        alarmDao.getEnabledAlarms().forEach { scheduler.schedule(it) }
    }
}
