package com.dailydivine.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Screen S06 (PRD Section 9): Onboarding 5/5. */
@Composable
fun PermissionScreen(isCompleting: Boolean, onDone: (notificationsGranted: Boolean) -> Unit) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> onDone(granted) }
    )

    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Pre-API 33: no runtime prompt exists, notifications are
            // allowed by default unless the user disables them in Settings.
            onDone(true)
        }
    }

    OnboardingScaffold(
        step = 5,
        title = "Stay Connected to Your Faith",
        subtitle = "We'll remind you of your daily verse and celebrate your streaks.",
        actions = {
            if (isCompleting) {
                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = { requestNotifications() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text("Allow Notifications") }
                TextButton(onClick = { onDone(false) }, modifier = Modifier.fillMaxWidth()) { Text("Maybe Later") }
            }
        }
    ) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            HeroMedal(Icons.Filled.Notifications, size = 96.dp)
        }
        Spacer(Modifier.height(24.dp))
        FeatureRow(Icons.Filled.WbSunny, "Your verse each morning", "A quiet notification at a time you choose")
        FeatureRow(Icons.Filled.LocalFireDepartment, "A gentle evening nudge", "Only if your streak is at risk, never spammy")
        FeatureRow(Icons.Filled.EmojiEvents, "Milestone celebrations", "Cheer when you reach a streak badge")
    }
}
