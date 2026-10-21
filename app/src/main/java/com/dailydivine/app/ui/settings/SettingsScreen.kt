package com.dailydivine.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import com.dailydivine.app.notifications.NotificationHelper
import com.dailydivine.app.ui.components.TimePickerDialog
import com.dailydivine.app.util.formatTime12h
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.util.AppLinks
import com.dailydivine.app.util.ContentCredits
import com.dailydivine.app.util.Religions
import com.dailydivine.app.wallpaper.WallpaperTarget

/**
 * Screen S12 (PRD Section 9): Settings. Most sections (Alarm config lives
 * on its own tab now, Audio/Appearance/Notifications/Premium/About) are
 * Sprint 7+ scope -- this covers Profile (read-only for now) and Data
 * (F012-R16 clear-all) since those are already fully backed by real data.
 */
@Composable
fun SettingsScreen(
    onDataCleared: () -> Unit,
    onOpenWallpaper: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val religion by viewModel.religion.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    var showReligionPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    val prefs by viewModel.prefs.collectAsState()
    val context = LocalContext.current
    val systemNotificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
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

        Spacer(Modifier.height(24.dp))
        Text("Personalise", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        ListItem(
            modifier = Modifier.clickable(onClick = onOpenWallpaper),
            headlineContent = { Text("Daily wallpaper") },
            supportingContent = {
                val target = WallpaperTarget.fromName(prefs.wallpaperTarget)
                Text(if (target == WallpaperTarget.OFF) "Off" else "On: ${target.label}")
            },
            trailingContent = { Text("Set up", color = MaterialTheme.colorScheme.primary) }
        )

        Spacer(Modifier.height(24.dp))
        Text("Notifications", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (!systemNotificationsOn) {
            // F009: the toggles below only take effect if the system allows
            // notifications for the app (Android 13+ asks at onboarding).
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Notifications are turned off for DailyDivine in system settings, so none of these will appear.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) { Text("Open system settings") }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        ListItem(
            modifier = Modifier.toggleable(
                value = prefs.dailyVerseEnabled, role = Role.Switch, onValueChange = viewModel::setDailyVerseEnabled
            ),
            headlineContent = { Text("Daily verse") },
            supportingContent = { Text("A morning notification with your verse") },
            trailingContent = {
                Switch(checked = prefs.dailyVerseEnabled, onCheckedChange = null)
            }
        )
        if (prefs.dailyVerseEnabled) {
            ListItem(
                modifier = Modifier.clickable { showTimePicker = true },
                headlineContent = { Text("Delivery time") },
                supportingContent = { Text(formatTime12h(prefs.dailyVerseHour, prefs.dailyVerseMinute)) },
                trailingContent = { Text("Change", color = MaterialTheme.colorScheme.primary) }
            )
        }
        ListItem(
            modifier = Modifier.toggleable(
                value = prefs.streakReminderEnabled, role = Role.Switch, onValueChange = viewModel::setStreakReminderEnabled
            ),
            headlineContent = { Text("Streak reminder") },
            supportingContent = { Text("8 PM nudge if you haven't opened the app and your streak is over 3 days") },
            trailingContent = {
                Switch(checked = prefs.streakReminderEnabled, onCheckedChange = null)
            }
        )
        ListItem(
            modifier = Modifier.toggleable(
                value = prefs.milestonesEnabled, role = Role.Switch, onValueChange = viewModel::setMilestonesEnabled
            ),
            headlineContent = { Text("Milestones") },
            supportingContent = { Text("Celebrate when you reach a streak milestone") },
            trailingContent = {
                Switch(checked = prefs.milestonesEnabled, onCheckedChange = null)
            }
        )

        Spacer(Modifier.height(24.dp))
        Text("Voice", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        ListItem(
            headlineContent = { Text("Reading speed") },
            supportingContent = { Text("How fast verses are read aloud") }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            listOf("Slow" to 0.75f, "Normal" to 1.0f, "Fast" to 1.25f).forEach { (label, rate) ->
                FilterChip(
                    selected = prefs.ttsRate == rate,
                    onClick = { viewModel.setTtsRate(rate) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("About", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        ListItem(
            modifier = Modifier.clickable { openLink(context, AppLinks.PRIVACY_URL) },
            headlineContent = { Text("Privacy policy") },
            trailingContent = { Text("Open", color = MaterialTheme.colorScheme.primary) }
        )
        ListItem(
            modifier = Modifier.clickable { openLink(context, AppLinks.TERMS_URL) },
            headlineContent = { Text("Terms of use") },
            trailingContent = { Text("Open", color = MaterialTheme.colorScheme.primary) }
        )
        ListItem(
            modifier = Modifier.clickable { showCredits = true },
            headlineContent = { Text("Content sources & credits") },
            trailingContent = { Text("View", color = MaterialTheme.colorScheme.primary) }
        )
        ListItem(
            modifier = Modifier.clickable { sendSupportEmail(context) },
            headlineContent = { Text("Contact support") },
            supportingContent = { Text(AppLinks.SUPPORT_EMAIL) }
        )
        ListItem(
            headlineContent = { Text("Version") },
            supportingContent = { Text(appVersionName(context)) }
        )

        if ((context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // Debug builds only: preview all three notification types now.
            TextButton(onClick = {
                NotificationHelper.postDailyVerse(
                    context,
                    "The LORD is my shepherd; I shall not want. He maketh me to lie down in green pastures.",
                    "Test"
                )
                NotificationHelper.postStreakReminder(context, 5)
                NotificationHelper.postMilestone(context, 7, "Week Warrior")
            }) { Text("Debug: send test notifications now") }
        }

        Spacer(Modifier.height(32.dp))

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

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            title = { Text("Content sources & credits") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    ContentCredits.SCRIPTURE.forEach { (tradition, source) ->
                        Text(tradition, style = MaterialTheme.typography.titleMedium)
                        Text(source, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(ContentCredits.OTHER, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showCredits = false }) { Text("Close") } }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = prefs.dailyVerseHour,
            initialMinute = prefs.dailyVerseMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute -> viewModel.setDailyVerseTime(hour, minute) }
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

/** Opens a web page in the user's browser. The app itself makes no network requests. */
private fun openLink(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "No web browser found on this phone", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun sendSupportEmail(context: android.content.Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:${AppLinks.SUPPORT_EMAIL}"))
                .putExtra(Intent.EXTRA_SUBJECT, "DailyDivine support")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "No email app found on this phone", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun appVersionName(context: android.content.Context): String =
    try {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
