package com.dailydivine.app.ui.onboarding

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
 * NOTE: this records the user's stated preference but does not yet trigger
 * Android's real POST_NOTIFICATIONS runtime permission dialog (API 33+) --
 * that needs an ActivityResultContracts.RequestPermission launcher wired
 * from MainActivity. Tracked as a follow-up in CHECKLIST.md; deferred so
 * this screen doesn't block on Activity-level plumbing in this pass.
 */
@Composable
fun PermissionScreen(isCompleting: Boolean, onDone: (notificationsGranted: Boolean) -> Unit) {
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
            Button(onClick = { onDone(true) }, modifier = Modifier.fillMaxWidth()) { Text("Allow Notifications") }
            TextButton(onClick = { onDone(false) }, modifier = Modifier.fillMaxWidth()) { Text("Maybe Later") }
        }
    }
}
