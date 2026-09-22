package com.dailydivine.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen S12 (PRD Section 9): Settings. Most sections (Alarm config lives
 * on its own tab now, Audio/Appearance/Notifications/Premium/About) are
 * Sprint 7+ scope -- this covers Profile (read-only for now) and Data
 * (F012-R16 clear-all) since those are already fully backed by real data.
 */
@Composable
fun SettingsScreen(onDataCleared: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val religion by viewModel.religion.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        ListItem(
            headlineContent = { Text("Religion") },
            supportingContent = { Text(religion?.name ?: "Not set") }
        )
        ListItem(
            headlineContent = { Text("Language") },
            supportingContent = { Text(languageCode) }
        )
        // Editing religion/language after onboarding (F001-R04) is a
        // follow-up -- these are read-only for now.

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear all data")
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This resets your religion, language, and onboarding progress. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    viewModel.clearAllData(onDataCleared)
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
