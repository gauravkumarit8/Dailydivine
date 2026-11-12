package com.dailydivine.app.ui.settings

import com.dailydivine.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.dailydivine.app.ui.components.dialogMaxHeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
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
import com.dailydivine.app.util.LanguageSupport
import com.dailydivine.app.util.Religions
import com.dailydivine.app.wallpaper.WallpaperTarget

/**
 * Screen S12 (PRD Section 9): Settings. Most sections (Alarm config lives
 * on its own tab now, Audio/Appearance/Notifications/Premium/About) are
 * Sprint 7+ scope -- this covers Profile (read-only for now) and Data
 * (F012-R16 clear-all) since those are already fully backed by real data.
 */
@OptIn(ExperimentalLayoutApi::class) // FlowRow wraps the speed chips at large fonts
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
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    val prefs by viewModel.prefs.collectAsState()
    val context = LocalContext.current
    val systemNotificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 4.dp, vertical = 24.dp)) {
        // ListItem rows indent their text by 16 dp, so the root margin is 4 dp and the headings and
        // cards add 16 dp: everything lines up at 20 dp, the same margin as the other tabs.
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(24.dp))

        // Every other group has a heading; this first one did not.
        Text(stringResource(R.string.your_tradition), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
        SettingsRow(
            modifier = Modifier.clickable { showReligionPicker = true },
            headlineContent = { Text(stringResource(R.string.religion)) },
            supportingContent = { Text(religion?.name ?: "Not set") },
            trailingContent = { Text(stringResource(R.string.change), color = MaterialTheme.colorScheme.primary) }
        )
        GroupDivider()
        val languageOptions = LanguageSupport.optionsFor(religion?.id)
        SettingsRow(
            // Only tappable when the current tradition has more than one language (Hinduism: English and Hindi).
            modifier = if (languageOptions.size > 1) Modifier.clickable { showLanguagePicker = true } else Modifier,
            headlineContent = { Text(stringResource(R.string.language)) },
            supportingContent = { Text(languageDisplayName(languageCode)) },
            trailingContent = if (languageOptions.size > 1) {
                { Text(stringResource(R.string.change), color = MaterialTheme.colorScheme.primary) }
            } else null
        )
        }
        // Religion is editable (F001-R04). Language is editable only for traditions that have
        // verses in more than English (see LanguageSupport).

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.personalise), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
        SettingsRow(
            modifier = Modifier.clickable(onClick = onOpenWallpaper),
            headlineContent = { Text(stringResource(R.string.daily_wallpaper)) },
            supportingContent = {
                val target = WallpaperTarget.fromName(prefs.wallpaperTarget)
                Text(if (target == WallpaperTarget.OFF) "Off" else "On: ${target.label}")
            },
            trailingContent = { Text(stringResource(R.string.set_up), color = MaterialTheme.colorScheme.primary) }
        )
        }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.notifications), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        if (!systemNotificationsOn) {
            // F009: the toggles below only take effect if the system allows
            // notifications for the app (Android 13+ asks at onboarding).
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.notifications_are_turned_off_for),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.open_system_settings)) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        SettingsGroup {
        SettingsRow(
            modifier = Modifier.toggleable(
                value = prefs.dailyVerseEnabled, role = Role.Switch, onValueChange = viewModel::setDailyVerseEnabled
            ),
            headlineContent = { Text(stringResource(R.string.daily_verse)) },
            supportingContent = { Text(stringResource(R.string.a_morning_notification_with_your)) },
            trailingContent = {
                Switch(checked = prefs.dailyVerseEnabled, onCheckedChange = null)
            }
        )
        if (prefs.dailyVerseEnabled) {
            GroupDivider()
            SettingsRow(
                modifier = Modifier.clickable { showTimePicker = true },
                headlineContent = { Text(stringResource(R.string.delivery_time)) },
                supportingContent = { Text(formatTime12h(prefs.dailyVerseHour, prefs.dailyVerseMinute)) },
                trailingContent = { Text(stringResource(R.string.change), color = MaterialTheme.colorScheme.primary) }
            )
        }
        GroupDivider()
        SettingsRow(
            modifier = Modifier.toggleable(
                value = prefs.streakReminderEnabled, role = Role.Switch, onValueChange = viewModel::setStreakReminderEnabled
            ),
            headlineContent = { Text(stringResource(R.string.streak_reminder)) },
            supportingContent = { Text(stringResource(R.string.s_8_pm_nudge_if_you)) },
            trailingContent = {
                Switch(checked = prefs.streakReminderEnabled, onCheckedChange = null)
            }
        )
        GroupDivider()
        SettingsRow(
            modifier = Modifier.toggleable(
                value = prefs.milestonesEnabled, role = Role.Switch, onValueChange = viewModel::setMilestonesEnabled
            ),
            headlineContent = { Text(stringResource(R.string.milestones)) },
            supportingContent = { Text(stringResource(R.string.celebrate_when_you_reach_a)) },
            trailingContent = {
                Switch(checked = prefs.milestonesEnabled, onCheckedChange = null)
            }
        )
        }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.voice), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
        SettingsRow(
            headlineContent = { Text(stringResource(R.string.reading_speed)) },
            supportingContent = { Text(stringResource(R.string.how_fast_verses_are_read)) }
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
        Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.about), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
        SettingsRow(
            modifier = Modifier.clickable { openLink(context, AppLinks.PRIVACY_URL) },
            headlineContent = { Text(stringResource(R.string.privacy_policy)) },
            trailingContent = { Text(stringResource(R.string.open), color = MaterialTheme.colorScheme.primary) }
        )
        GroupDivider()
        SettingsRow(
            modifier = Modifier.clickable { openLink(context, AppLinks.TERMS_URL) },
            headlineContent = { Text(stringResource(R.string.terms_of_use)) },
            trailingContent = { Text(stringResource(R.string.open), color = MaterialTheme.colorScheme.primary) }
        )
        GroupDivider()
        SettingsRow(
            modifier = Modifier.clickable { showCredits = true },
            headlineContent = { Text(stringResource(R.string.content_sources_credits)) },
            trailingContent = { Text(stringResource(R.string.view), color = MaterialTheme.colorScheme.primary) }
        )
        GroupDivider()
        SettingsRow(
            modifier = Modifier.clickable { sendSupportEmail(context) },
            headlineContent = { Text(stringResource(R.string.contact_support)) },
            supportingContent = { Text(AppLinks.SUPPORT_EMAIL) }
        )
        GroupDivider()
        SettingsRow(
            headlineContent = { Text(stringResource(R.string.version)) },
            supportingContent = { Text(appVersionName(context)) }
        )
        }

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
            }) { Text(stringResource(R.string.debug_send_test_notifications_now)) }
        }

        Spacer(Modifier.height(32.dp))

        OutlinedButton(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.clear_all_data))
        }
    }

    if (showReligionPicker) {
        AlertDialog(
            onDismissRequest = { showReligionPicker = false },
            title = { Text(stringResource(R.string.choose_your_path)) },
            text = {
                Column {
                    Religions.ALL.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable(enabled = r.hasContent) {
                                    showReligionPicker = false
                                    viewModel.changeReligion(r.id)
                                }
                        ) {
                            RadioButton(selected = religion?.id == r.id, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(r.name, style = MaterialTheme.typography.bodyLarge)
                                if (r.contentAssetEn == null) {
                                    Text(stringResource(R.string.verses_coming_soon), style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showReligionPicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showLanguagePicker) {
        val options = LanguageSupport.optionsFor(religion?.id)
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text(stringResource(R.string.choose_verse_language)) },
            text = {
                Column {
                    options.forEach { (name, code) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(selected = languageCode == code, role = Role.RadioButton) {
                                    showLanguagePicker = false
                                    viewModel.setLanguage(code)
                                }
                        ) {
                            RadioButton(selected = languageCode == code, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.hindi_gita_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showLanguagePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            title = { Text(stringResource(R.string.content_sources_credits)) },
            text = {
                Column(Modifier.heightIn(max = dialogMaxHeight()).verticalScroll(rememberScrollState())) {
                    ContentCredits.SCRIPTURE.forEach { (tradition, source) ->
                        Text(tradition, style = MaterialTheme.typography.titleMedium)
                        Text(source, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(ContentCredits.OTHER, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showCredits = false }) { Text(stringResource(R.string.close)) } }
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
            title = { Text(stringResource(R.string.clear_all_data_2)) },
            text = { Text(stringResource(R.string.this_permanently_deletes_your_alarms)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    viewModel.clearAllData(onDataCleared)
                }) { Text(stringResource(R.string.clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.cancel)) }
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

/** "en" -> "English" (the raw language code was shown before). */
private fun languageDisplayName(code: String): String =
    java.util.Locale.forLanguageTag(code).getDisplayLanguage(java.util.Locale.ENGLISH)
        .replaceFirstChar { it.uppercase() }
        .ifBlank { code }

/** One section's rows in a single rounded card, with the section heading above it. */
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(vertical = 4.dp), content = content)
    }
}

/** A ListItem that sits on the group card instead of painting its own page-coloured background. */
@Composable
private fun SettingsRow(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun GroupDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}
