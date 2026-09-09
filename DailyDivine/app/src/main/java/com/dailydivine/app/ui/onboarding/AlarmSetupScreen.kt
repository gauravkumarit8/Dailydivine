package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Screen S05 (PRD Section 9): Onboarding 4/5 — defaults to 5:30 AM.
 *
 * v1.1 (F004-R25): this is also where the Android 12+ exact-alarm
 * permission is requested, before the first alarm is scheduled. The actual
 * system settings intent launch is wired in AlarmSetupViewModel once Hilt
 * navigation lands (Sprint 4); [onRequestExactAlarmPermission] is exposed
 * here so the composable stays testable without a real Activity.
 */
@Composable
fun AlarmSetupScreen(
    canScheduleExactAlarms: Boolean = true,
    onRequestExactAlarmPermission: () -> Unit = {},
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var ttsEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Set Your Morning Blessing", style = MaterialTheme.typography.headlineMedium)
        Text("Wake up to divine wisdom every day", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Text("Default alarm time: 5:30 AM", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Read verse aloud when alarm rings", modifier = Modifier.weight(1f))
            Switch(checked = ttsEnabled, onCheckedChange = { ttsEnabled = it })
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
                    TextButton(onClick = onRequestExactAlarmPermission) { Text("Enable exact alarms") }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Set Alarm →") }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Skip, I'll set up later") }
    }
}
