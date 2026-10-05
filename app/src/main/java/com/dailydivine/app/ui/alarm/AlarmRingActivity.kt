package com.dailydivine.app.ui.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.sp
import com.dailydivine.app.ui.components.rememberReduceMotion
import com.dailydivine.app.ui.theme.VerseTextStyle
import com.dailydivine.app.ui.theme.darkenUntilContrast
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
        stopAlarmService() // silence first; the lookup below must never delay that
        lifecycleScope.launch {
            // F004-R18: if this alarm has TTS on, Home reads the verse aloud once.
            // A failed lookup just means no auto-read; it must never block waking up.
            val readAloud = runCatching { alarmRepository.getAlarmById(alarmId)?.isTTSEnabled == true }
                .getOrDefault(false)
            startActivity(
                Intent(this@AlarmRingActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    if (readAloud) putExtra(MainActivity.EXTRA_AUTO_READ, true)
                }
            )
            finish()
        }
    }
}

@Composable
private fun AlarmRingScreen(
    currentTime: String,
    versePreview: String?,
    onSnooze: () -> Unit,
    onWakeUpAndRead: () -> Unit
) {
    // S08: religion-themed gradient (primary fading to a deeper shade of itself).
    // All text here is white, so the gradient's lightest colour must give white >= 4.5:1,
    // in light AND dark theme (dark theme's primary is lightened for text elsewhere).
    val top = darkenUntilContrast(MaterialTheme.colorScheme.primary, listOf(Color.White))
    val gradient = Brush.verticalGradient(listOf(top, lerp(top, Color.Black, 0.45f)))
    val reduceMotion = rememberReduceMotion()

    // S08: gentle pulse on the primary button.
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 1.04f, // A11Y-09
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            currentTime,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 64.sp, lineHeight = 72.sp),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Time for your morning blessing",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // F004-R15: verse preview (first line of today's verse), when one
        // is available -- null while loading or if the user's religion has
        // no sample content yet (same graceful empty state as Home).
        versePreview?.let {
            Spacer(Modifier.height(32.dp))
            Text(
                "\u201C$it\u201D",
                style = VerseTextStyle.copy(fontSize = 18.sp, lineHeight = 28.sp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(if (versePreview != null) 56.dp else 72.dp))

        Button(
            onClick = onWakeUpAndRead,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = top),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
        ) {
            Text("Wake Up & Read", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onSnooze,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text("Snooze", style = MaterialTheme.typography.titleMedium)
        }
    }
}
