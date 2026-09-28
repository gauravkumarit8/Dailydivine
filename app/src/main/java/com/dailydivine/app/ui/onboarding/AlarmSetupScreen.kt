package com.dailydivine.app.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dailydivine.app.ui.components.TimePickerDialog
import com.dailydivine.app.util.formatTime12h

/**
 * Screen S05 (PRD Section 9): Onboarding 4/5 — defaults to 5:30 AM.
 *
 * v1.1 (F004-R25): this is also where the Android 12+ exact-alarm
 * permission is requested, before the first alarm is scheduled.
 */
@Composable
fun AlarmSetupScreen(
    alarmHour: Int,
    alarmMinute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    ttsEnabled: Boolean,
    onTtsToggle: (Boolean) -> Unit,
    canScheduleExactAlarms: Boolean,
    onRequestExactAlarmPermission: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // BUG FIX: the button below previously only called
    // onRequestExactAlarmPermission() (a status re-check with nothing to
    // re-check, since no Settings screen had ever been opened) -- it never
    // actually gave the user anywhere to grant the permission. Every alarm
    // was therefore silently running through AlarmScheduler's setWindow()
    // fallback (up to a 10-minute delivery window) instead of the exact
    // AlarmClockInfo path, which is exactly what "alarm doesn't fire at the
    // set time" looks like in practice. Fixed: the button now launches the
    // real system settings screen, and this effect re-checks permission
    // status automatically whenever the user returns to the app (e.g. after
    // granting it in Settings and pressing back), so the banner and every
    // alarm scheduled afterward reflect the real, current state.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRequestExactAlarmPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Set Your Morning Blessing", style = MaterialTheme.typography.headlineMedium)
        Text("Wake up to divine wisdom every day", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatTime12h(alarmHour, alarmMinute), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = { showTimePicker = true }) { Text("Change") }
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Read verse aloud when alarm rings", modifier = Modifier.weight(1f))
            Switch(checked = ttsEnabled, onCheckedChange = onTtsToggle)
        }

        // v1.1 / F004-R25: graceful reliability banner instead of a silent
        // downgrade to inexact delivery when exact-alarm permission is denied.
        if (!canScheduleExactAlarms) {
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "For the most reliable wake-up time, allow DailyDivine to schedule exact alarms.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        }
                    }) { Text("Enable exact alarms") }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Set Alarm →") }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Skip, I'll set up later") }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = alarmHour,
            initialMinute = alarmMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = onTimeChange
        )
    }
}
