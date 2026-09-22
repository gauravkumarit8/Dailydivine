package com.dailydivine.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Screen S06 (PRD Section 9): Onboarding 5/5 — notification permission,
 * then routes to Home.
 *
 * Triggers Android's real POST_NOTIFICATIONS runtime permission dialog on
 * API 33+ (below that, notification permission is granted at install time,
 * no runtime prompt exists). rememberLauncherForActivityResult works
 * directly in a composable -- registration just needs to happen during
 * composition, which this satisfies without any MainActivity plumbing.
 */
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

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Stay Connected to Your Faith", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "We'll remind you of your daily verse and celebrate your streaks.",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        if (isCompleting) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { requestNotifications() }, modifier = Modifier.fillMaxWidth()) { Text("Allow Notifications") }
            TextButton(onClick = { onDone(false) }, modifier = Modifier.fillMaxWidth()) { Text("Maybe Later") }
        }
    }
}
