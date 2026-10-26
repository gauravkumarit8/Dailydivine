package com.dailydivine.app.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import android.os.*
import androidx.core.app.NotificationCompat
import com.dailydivine.app.ui.alarm.AlarmRingActivity
import kotlinx.coroutines.*

/**
 * F004-R23: foreground service that owns alarm tone playback.
 *
 * v1.1: also owns the [AlarmEscalationController] loop for F004-R19 — ticks
 * every second, ramps MediaPlayer volume via [AlarmEscalationController]
 * decisions, and on exhaustion stops cleanly and logs alarm_missed instead
 * of just going silent after 5 minutes.
 *
 * Launches [AlarmRingActivity] (Screen S08) via the notification's
 * full-screen intent -- the officially recommended mechanism for this
 * exact case (an alarm/call-style interruption), rather than calling
 * startActivity() directly from the service, which is subject to Android
 * 10+'s background-activity-launch restrictions and can silently fail to
 * show anything.
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
    private var snoozeMinutes: Int = AlarmScheduler.DEFAULT_SNOOZE_MINUTES

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                // Sent by AlarmRingActivity when the user taps Snooze or
                // Wake Up & Read -- stop cleanly rather than escalating further.
                // (The Activity schedules or cancels the snooze itself.)
                stopTickerAndTone()
                AlarmNotifications.clear(applicationContext)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE -> {
                // Notification action: snooze without opening the ring screen.
                val id = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
                val tone = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_TONE) ?: "temple_bell"
                val minutes = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, AlarmScheduler.DEFAULT_SNOOZE_MINUTES)
                    ?: AlarmScheduler.DEFAULT_SNOOZE_MINUTES
                stopTickerAndTone()
                if (id >= 0) {
                    AlarmScheduler(applicationContext).scheduleSnooze(
                        alarm = com.dailydivine.app.data.local.entity.Alarm(
                            id = id, hour = 0, minute = 0, repeatDays = "[]",
                            alarmToneId = tone, snoozeDurationMinutes = minutes, createdAt = 0L
                        ),
                        minutesFromNow = minutes
                    )
                }
                AlarmNotifications.clear(applicationContext)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_DISMISS -> {
                // Notification action: stop for good, including any pending auto-snooze.
                val id = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
                stopTickerAndTone()
                if (id >= 0) AlarmScheduler(applicationContext).cancelSnooze(id)
                AlarmNotifications.clear(applicationContext)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        toneId = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_TONE) ?: "temple_bell"
        // Auto-snooze cycles are carried in the re-fire Intent: this service
        // instance is destroyed between cycles, so an in-memory counter would
        // restart at 0 every time and the 6-cycle cap could never trigger.
        cyclesUsed = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_CYCLES, 0) ?: 0
        snoozeMinutes = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, AlarmScheduler.DEFAULT_SNOOZE_MINUTES)
            ?: AlarmScheduler.DEFAULT_SNOOZE_MINUTES

        startForeground(
            AlarmNotifications.NOTIFICATION_ID,
            AlarmNotifications.build(this, alarmId, toneId, snoozeMinutes)
        )
        // A second alarm (or a snooze) can start while another is still ringing. Release the
        // earlier player and wake lock first: overwriting the reference orphaned a looping
        // MediaPlayer that nothing could ever stop.
        stopTickerAndTone()
        acquireWakeLock()
        startTone()
        // Each (re-)fire is a fresh ring cycle for escalation timing.
        ringStartElapsedMs = SystemClock.elapsedRealtime()
        startEscalationTicker()

        return START_NOT_STICKY
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
                                    alarmToneId = toneId, snoozeDurationMinutes = snoozeMinutes, createdAt = 0L
                                ),
                                minutesFromNow = decision.minutes,
                                cyclesUsed = decision.cyclesUsedAfterThis
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

    /**
     * An alarm must ALWAYS make noise. Try the bundled tone first; if it
     * can't be prepared for any reason (bad/missing asset, codec error), fall
     * back to the system default alarm sound rather than crashing the
     * service or ringing silently.
     */
    private fun startTone() {
        val bundled = android.net.Uri.parse("android.resource://$packageName/${resolveToneResource(toneId)}")
        mediaPlayer = try {
            createPlayer(bundled)
        } catch (e: Exception) {
            Log.e(TAG, "Bundled alarm tone failed, falling back to system alarm sound", e)
            try {
                val fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                createPlayer(fallback)
            } catch (e2: Exception) {
                Log.e(TAG, "System fallback tone also failed", e2)
                null
            }
        }
    }

    private fun createPlayer(uri: android.net.Uri): MediaPlayer {
        val player = MediaPlayer()
        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            player.setDataSource(applicationContext, uri)
            player.isLooping = true
            player.setVolume(0.15f, 0.15f) // start quiet; escalation ramps this up
            player.prepare()
            player.start()
            return player
        } catch (e: Exception) {
            player.release()
            throw e
        }
    }

    private fun applyVolume(fraction: Float) {
        val v = (0.15f + 0.85f * fraction).coerceIn(0f, 1f)
        mediaPlayer?.setVolume(v, v)
    }

    private fun resolveToneResource(toneId: String): Int = AlarmTones.resIdFor(toneId)

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "DailyDivine:AlarmWakeLock"
        ).apply { acquire(35 * 60 * 1000L) } // slightly > 30-min escalation window as a safety cap
    }

    private fun stopTickerAndTone() {
        tickJob?.cancel()
        try { mediaPlayer?.stop() } catch (e: IllegalStateException) { Log.w(TAG, "stop() before start", e) }
        mediaPlayer?.release()
        mediaPlayer = null
        wakeLock?.let { if (it.isHeld) it.release() }
    }

    override fun onDestroy() {
        stopTickerAndTone()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AlarmService"
        const val ACTION_STOP = "com.dailydivine.app.alarm.ACTION_STOP"
        const val ACTION_SNOOZE = "com.dailydivine.app.alarm.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.dailydivine.app.alarm.ACTION_DISMISS"
    }
}
