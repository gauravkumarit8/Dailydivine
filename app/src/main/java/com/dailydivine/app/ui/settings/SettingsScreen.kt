package com.dailydivine.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.util.Religions

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
    var showReligionPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        ListItem(
            modifier = Modifier.clickable { showReligionPicker = true },
            headlineContent = { Text("Religion") },
            supportingContent = { Text(religion?.name ?: "Not set") },
            trailingContent = { Text("Change", color = MaterialTheme.colorScheme.primary) }
        )
        ListItem(
            headlineContent = { Text("Language") },
            supportingContent = { Text(languageCode) }
        )
        // Religion is editable (F001-R04). Language stays read-only until
        // more than English content/UI strings exist.

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear all data")
        }
    }

    if (showReligionPicker) {
        AlertDialog(
            onDismissRequest = { showReligionPicker = false },
            title = { Text("Choose your path") },
            text = {
                Column {
                    Religions.ALL.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    showReligionPicker = false
                                    viewModel.changeReligion(r.id)
                                }
                        ) {
                            RadioButton(selected = religion?.id == r.id, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(r.name, style = MaterialTheme.typography.bodyLarge)
                                if (r.contentAssetEn == null) {
                                    Text("Verses coming soon", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showReligionPicker = false }) { Text("Cancel") } }
        )
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
