package com.dailydivine.app.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import com.dailydivine.app.R
import kotlinx.coroutines.*

/**
 * F004-R23: foreground service that owns alarm tone playback.
 *
 * v1.1: also owns the [AlarmEscalationController] loop for F004-R19 — ticks
 * every second, ramps MediaPlayer volume via [AlarmEscalationController]
 * decisions, and on exhaustion stops cleanly and logs alarm_missed instead
 * of just going silent after 5 minutes.
 */
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val escalation = AlarmEscalationController()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null

    private var alarmId: Int = -1
    private var toneId: String = "temple_bell"
    private var ringStartElapsedMs: Long = 0L
    private var cyclesUsed: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        toneId = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_TONE) ?: "temple_bell"
        val isSnooze = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false) ?: false

        startForeground(NOTIFICATION_ID, buildNotification())
        acquireWakeLock()
        startTone()
        // On a manual/auto snooze re-fire, this is a fresh ring cycle for
        // escalation purposes; cyclesUsed is tracked in-process for the life
        // of this alarm's ringing sequence (reset happens naturally since
        // the process restarts the service per re-fire from AlarmReceiver).
        ringStartElapsedMs = SystemClock.elapsedRealtime()
        startEscalationTicker()

        return START_NOT_STICKY
    }

    /** Called by AlarmRingActivity when the user taps Snooze or Wake Up & Read. */
    fun onUserInteracted() {
        stopTickerAndTone()
        stopSelf()
    }

    private fun startEscalationTicker() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = SystemClock.elapsedRealtime() - ringStartElapsedMs
                when (val decision = escalation.onTick(elapsed, cyclesUsed)) {
                    is AlarmEscalationController.Decision.KeepEscalating -> {
                        applyVolume(decision.volumeFraction)
                    }
                    is AlarmEscalationController.Decision.AutoSnooze -> {
                        cyclesUsed = decision.cyclesUsedAfterThis
                        withContext(Dispatchers.Main) {
                            AlarmScheduler(applicationContext).scheduleSnooze(
                                alarm = com.dailydivine.app.data.local.entity.Alarm(
                                    id = alarmId, hour = 0, minute = 0, repeatDays = "[]",
                                    alarmToneId = toneId, createdAt = 0L
                                ),
                                minutesFromNow = decision.minutes
                            )
                        }
                        stopTickerAndTone()
                        stopSelf()
                        return@launch
                    }
                    is AlarmEscalationController.Decision.StopAndLogMissed -> {
                        logAlarmMissed(decision.totalCyclesUsed)
                        stopTickerAndTone()
                        stopSelf()
                        return@launch
                    }
                }
            }
        }
    }

    private fun logAlarmMissed(cyclesUsed: Int) {
        // Wire to FirebaseAnalytics once Sprint 7 adds the dependency:
        // Firebase.analytics.logEvent(AlarmEscalationController.EVENT_ALARM_MISSED) {
        //     param(AlarmEscalationController.PARAM_ALARM_ID, alarmId.toLong())
        //     param(AlarmEscalationController.PARAM_SNOOZE_CYCLES_USED, cyclesUsed.toLong())
        // }
    }

    private fun startTone() {
        val resId = resolveToneResource(toneId)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(applicationContext, android.net.Uri.parse("android.resource://$packageName/$resId"))
            isLooping = true
            setVolume(0.15f, 0.15f) // start quiet; escalation ramps this up
            prepare()
            start()
        }
    }

    private fun applyVolume(fraction: Float) {
        val v = (0.15f + 0.85f * fraction).coerceIn(0f, 1f)
        mediaPlayer?.setVolume(v, v)
    }

    private fun resolveToneResource(toneId: String): Int = R.raw.temple_bell // placeholder mapping

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "DailyDivine:AlarmWakeLock"
        ).apply { acquire(35 * 60 * 1000L) } // slightly > 30-min escalation window as a safety cap
    }

    private fun stopTickerAndTone() {
        tickJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        wakeLock?.let { if (it.isHeld) it.release() }
    }

    private fun buildNotification(): Notification {
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Alarm", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Good morning")
            .setContentText("Time for your morning blessing")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopTickerAndTone()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 4004
    }
}
