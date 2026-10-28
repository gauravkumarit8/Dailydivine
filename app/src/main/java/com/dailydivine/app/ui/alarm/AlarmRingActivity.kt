package com.dailydivine.app.ui.alarm

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.dailydivine.app.alarm.RingSettings
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
        // Edge-to-edge is enforced at targetSdk 35+. This screen is always a dark gradient, so ask for
        // light system-bar icons in every theme (the default would follow the phone's light/dark mode
        // and could draw dark icons on the dark gradient).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
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
                    snoozeMinutes = RingSettings.fromIntent(intent).snoozeMinutes,
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

    /** Removes a snooze that is still pending (e.g. the screen sat unattended through an auto-snooze). */
    private fun cancelPendingSnooze() {
        if (alarmId >= 0) AlarmScheduler(this).cancelSnooze(alarmId)
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
        cancelPendingSnooze() // the user is awake: a leftover auto-snooze must not ring again
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
    snoozeMinutes: Int,
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

    // Responsive: this is the screen a half-asleep user must be able to operate on ANY phone, so
    //  - the clock + verse scroll when they do not fit (small phone, large font, landscape),
    //  - the two buttons are pinned and can never be pushed off-screen,
    //  - landscape (wide and short) puts the buttons beside the text instead of below it.
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(gradient).systemBarsPadding()
    ) {
        val landscape = maxWidth > maxHeight && maxWidth >= 480.dp
        val clockSize = if (maxWidth < 360.dp) 52.sp else 64.sp

        val details: @Composable ColumnScope.() -> Unit = {
            RingClock(currentTime, clockSize)
            Spacer(Modifier.height(12.dp))
            Text(
                "Time for your morning blessing",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            // F004-R15: verse preview (first line of today's verse), when one is available.
            versePreview?.let {
                Spacer(Modifier.height(24.dp))
                Text(
                    "\u201C$it\u201D",
                    style = VerseTextStyle.copy(fontSize = 18.sp, lineHeight = 28.sp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        val actions: @Composable ColumnScope.() -> Unit = {
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
                Text("Snooze \u2013 $snoozeMinutes min", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (landscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = details
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = actions
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = details
                    )
                }
                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth(), content = actions)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** The big clock. Its font scaling is capped at 1.3x (only for this one Text) so "10:30 AM" cannot
 *  wrap or clip at 200 % system font size; every other text on the screen still scales fully. */
@Composable
private fun RingClock(text: String, size: TextUnit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale = minOf(density.fontScale, 1.3f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = size, lineHeight = size * 1.125f),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}
