package com.dailydivine.app.ui.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.dailydivine.app.alarm.AlarmScheduler
import com.dailydivine.app.alarm.AlarmService
import com.dailydivine.app.data.repository.AlarmRepository
import com.dailydivine.app.ui.MainActivity
import com.dailydivine.app.ui.theme.DailyDivineTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Screen S08 (PRD Section 9): full-screen alarm ring UI, launched by
 * [AlarmService]'s full-screen notification intent when an alarm fires.
 *
 * Handles F004-R16/R17 (Snooze / "Wake Up & Read" buttons) and the API 26
 * fallback for showWhenLocked/turnScreenOn -- those `<activity>` manifest
 * attributes only take effect on API 27+ (see AndroidManifest.xml's
 * comment), so minSdk 26 needs the older WindowManager flags too.
 */
@AndroidEntryPoint
class AlarmRingActivity : ComponentActivity() {

    @Inject lateinit var alarmRepository: AlarmRepository

    private var alarmId: Int = -1
    private var toneId: String = "temple_bell"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLockScreenFlags()

        // F004-R14: back button must not silently dismiss an alarm without
        // going through Snooze or Wake Up & Read. Uses the modern
        // OnBackPressedCallback API rather than overriding the deprecated
        // onBackPressed() directly.
        onBackPressedDispatcher.addCallback(this) { /* absorb, no-op */ }

        alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        toneId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TONE) ?: "temple_bell"

        setContent {
            DailyDivineTheme {
                val ringViewModel: AlarmRingViewModel = hiltViewModel()
                val versePreview by ringViewModel.versePreview.collectAsState()
                AlarmRingScreen(
                    currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                    versePreview = versePreview,
                    onSnooze = { snooze() },
                    onWakeUpAndRead = { wakeUpAndRead() }
                )
            }
        }
    }

    /** API 26 fallback (follow-up to F004-R25): the manifest's
     *  showWhenLocked/turnScreenOn attributes only apply on API 27+. */
    private fun applyLockScreenFlags() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun stopAlarmService() {
        startService(Intent(this, AlarmService::class.java).apply { action = AlarmService.ACTION_STOP })
    }

    /** F004-R16: snooze reschedules for the alarm's configured duration. */
    private fun snooze() {
        lifecycleScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            stopAlarmService()
            alarm?.let {
                AlarmScheduler(this@AlarmRingActivity).scheduleSnooze(it, it.snoozeDurationMinutes)
            }
            finish()
        }
    }

    /** F004-R17: dismisses the alarm and opens the daily verse (Home). */
    private fun wakeUpAndRead() {
        stopAlarmService()
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )
        finish()
    }
}

@Composable
private fun AlarmRingScreen(
    currentTime: String,
    versePreview: String?,
    onSnooze: () -> Unit,
    onWakeUpAndRead: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(currentTime, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Time for your morning blessing",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        // F004-R15: verse preview (first line of today's verse), when one
        // is available -- null while loading or if the user's religion has
        // no sample content yet (same graceful empty state as Home).
        versePreview?.let {
            Spacer(Modifier.height(24.dp))
            Text(
                "\"$it\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(if (versePreview != null) 40.dp else 64.dp))

        Button(onClick = onWakeUpAndRead, modifier = Modifier.fillMaxWidth()) {
            Text("Wake Up & Read")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth()) {
            Text("Snooze")
        }
    }
}
