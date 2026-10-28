package com.dailydivine.app.ui.alarm

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dailydivine.app.alarm.AlarmScheduler
import com.dailydivine.app.alarm.RingSettings
import com.dailydivine.app.alarm.AlarmTones
import com.dailydivine.app.alarm.formatRingsIn
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dailydivine.app.data.local.entity.Alarm
import com.dailydivine.app.ui.components.TimePickerDialog
import com.dailydivine.app.util.formatTime12h

/** Screen S11 (PRD Section 9): Alarm list + configuration. */
@Composable
fun AlarmListScreen(viewModel: AlarmListViewModel = hiltViewModel()) {
    val alarms by viewModel.alarms.collectAsState()
    val canScheduleExactAlarms by viewModel.canScheduleExactAlarms.collectAsState()
    val isIgnoringBatteryOptimizations by viewModel.isIgnoringBatteryOptimizations.collectAsState()
    val notificationsReady by viewModel.notificationsReady.collectAsState()
    val fullScreenIntentReady by viewModel.fullScreenIntentReady.collectAsState()
    val alarmStreamAudible by viewModel.alarmStreamAudible.collectAsState()
    val religionId by viewModel.religionId.collectAsState()
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var editingToneFor by remember { mutableStateOf<Alarm?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keeps the "Rings in ..." line current while the screen is open.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            nowMs = System.currentTimeMillis()
        }
    }

    // Same fix as AlarmSetupScreen (onboarding): re-check exact-alarm
    // permission status whenever this screen resumes, e.g. after the user
    // comes back from the system Settings screen. Previously this tab had
    // no visibility into this at all -- a user who skipped it during
    // onboarding had no way to discover or fix it here.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshExactAlarmPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addAlarm() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add alarm")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (viewModel.isDebuggable) {
                TextButton(
                    onClick = { viewModel.fireTestAlarm(10) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) { Text("Debug: ring a test alarm in 10 seconds") }
            }
            // All reliability problems in ONE panel (a stack of five banners pushed the alarms off screen).
            val warnings = buildList {
                if (!canScheduleExactAlarms) add(
                    AlarmWarning(
                        "Exact alarms are turned off, so alarms may fire up to 10 minutes late.",
                        "Enable exact alarms"
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                )
                            }
                        }
                    }
                )
                if (!isIgnoringBatteryOptimizations) add(
                    AlarmWarning(
                        "Battery optimization may stop your alarm from firing in the background. " +
                            "Find DailyDivine in the list and choose \"Don't optimize\".",
                        "Open battery settings"
                    ) {
                        runCatching { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                    }
                )
                if (!notificationsReady) add(
                    AlarmWarning(
                        "Notifications are turned off, so the alarm screen may not open and you could " +
                            "not snooze or dismiss from the lock screen.",
                        "Open notification settings"
                    ) {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            )
                        }
                    }
                )
                if (!fullScreenIntentReady) add(
                    AlarmWarning(
                        "Full-screen alerts are turned off, so the alarm screen will not appear over your " +
                            "lock screen. You can still snooze or dismiss from the notification.",
                        "Allow full-screen alerts"
                    ) {
                        runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                context.startActivity(
                                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                )
                            }
                        }
                    }
                )
                if (!alarmStreamAudible) add(
                    AlarmWarning(
                        "Your phone's alarm volume is turned all the way down, so the alarm would ring silently.",
                        "Open sound settings"
                    ) {
                        runCatching { context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) }
                    }
                )
            }
            AlarmWarningsPanel(warnings)

            if (alarms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No alarms yet",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap + to add your first morning blessing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            nowMs = nowMs,
                            onDayToggle = { day -> viewModel.toggleDay(alarm, day) },
                            onToggle = { viewModel.toggleEnabled(alarm) },
                            onTimeClick = { editingAlarm = alarm },
                            onToneClick = { editingToneFor = alarm },
                            onTtsToggle = { viewModel.toggleTts(alarm) },
                            onVolumeChange = { percent -> viewModel.setVolume(alarm, percent) },
                            onGradualToggle = { viewModel.toggleGradual(alarm) },
                            onVibrationToggle = { viewModel.toggleVibration(alarm) },
                            onSnoozeSelect = { minutes -> viewModel.setSnoozeMinutes(alarm, minutes) },
                            onDelete = { viewModel.delete(alarm) }
                        )
                    }
                }
            }
        }
    }

    editingToneFor?.let { alarm ->
        TonePickerDialog(
            currentToneId = alarm.alarmToneId,
            religionId = religionId,
            onDismiss = { editingToneFor = null },
            onConfirm = { toneId ->
                viewModel.updateTone(alarm, toneId)
                editingToneFor = null
            }
        )
    }

    editingAlarm?.let { alarm ->
        TimePickerDialog(
            initialHour = alarm.hour,
            initialMinute = alarm.minute,
            onDismiss = { editingAlarm = null },
            onConfirm = { hour, minute -> viewModel.updateTime(alarm, hour, minute) }
        )
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    nowMs: Long,
    onDayToggle: (Int) -> Unit,
    onToggle: () -> Unit,
    onTimeClick: () -> Unit,
    onToneClick: () -> Unit,
    onTtsToggle: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onGradualToggle: () -> Unit,
    onVibrationToggle: () -> Unit,
    onSnoozeSelect: (Int) -> Unit,
    onDelete: () -> Unit
) {
    // Sound settings are collapsed by default so a list of alarms stays short and scannable.
    var soundOpen by rememberSaveable(alarm.id) { mutableStateOf(false) }
    val repeatDays = AlarmScheduler.parseRepeatDays(alarm.repeatDays)
    val subtitle = if (alarm.isEnabled) {
        val next = AlarmScheduler.computeNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays, 0L, nowMs)
        formatRingsIn(next - nowMs)
    } else {
        "Off"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 2.dp else 0.dp)
    ) {
        Column(Modifier.padding(20.dp).alpha(if (alarm.isEnabled) 1f else 0.6f)) {
            // toggleable on the whole row: TalkBack reads "7:00 AM, Morning, Rings in 7h, switch, on"
            // as ONE control instead of an unlabeled switch next to some text.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = alarm.isEnabled, role = Role.Switch, onValueChange = { onToggle() })
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        formatTime12h(alarm.hour, alarm.minute),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        "${alarm.label} \u00B7 $subtitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = alarm.isEnabled, onCheckedChange = null)
            }

            Spacer(Modifier.height(16.dp))
            // Seven equal-width cells (not seven fixed 40 dp circles, which overflowed 320 dp-wide
            // phones); each cell is at least 48 dp tall so it is an easy tap target.
            Row(modifier = Modifier.fillMaxWidth()) {
                DAY_LABELS.forEachIndexed { index, (letter, fullName) ->
                    val isoDay = index + 1
                    DayToggle(letter, fullName, selected = isoDay in repeatDays) { onDayToggle(isoDay) }
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onToneClick) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tone: ${AlarmTones.byId(alarm.alarmToneId).name}")
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            Spacer(Modifier.height(8.dp))

            // F004-R18: the verse is read aloud AFTER you tap "Wake Up & Read".
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = alarm.isTTSEnabled, role = Role.Switch, onValueChange = { onTtsToggle() })
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Read verse aloud", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Speaks today's verse after you tap Wake Up & Read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = alarm.isTTSEnabled, onCheckedChange = null)
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            TextButton(
                onClick = { soundOpen = !soundOpen },
                modifier = Modifier.semantics { stateDescription = if (soundOpen) "Expanded" else "Collapsed" }
            ) {
                Text(if (soundOpen) "Hide sound & feel" else "Sound & feel: volume, vibration, snooze")
            }
            if (soundOpen) {
                AlarmSoundSettings(alarm, onVolumeChange, onGradualToggle, onVibrationToggle, onSnoozeSelect)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onTimeClick) { Text("Change time") }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete alarm")
                }
            }
        }
    }
}

private val DAY_LABELS = listOf(
    "M" to "Monday", "T" to "Tuesday", "W" to "Wednesday", "T" to "Thursday",
    "F" to "Friday", "S" to "Saturday", "S" to "Sunday"
)

/** One weekday circle (F004-R08). 40 dp: seven fit across a phone-width card. */
@Composable
private fun RowScope.DayToggle(letter: String, fullName: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = if (selected) "$fullName, repeats" else "$fullName, off" },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) primary else primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                letter,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** One reliability problem: what is wrong and the single action that fixes it. */
private class AlarmWarning(val text: String, val buttonLabel: String, val onClick: () -> Unit)

/**
 * All alarm-reliability problems in one card. A single problem is shown in full; several are
 * collapsed behind one summary line ("3 things may stop your alarm") with a Fix/Hide button, so
 * the alarms themselves stay on screen.
 */
@Composable
private fun AlarmWarningsPanel(warnings: List<AlarmWarning>) {
    if (warnings.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val many = warnings.size > 1
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (many) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${warnings.size} things may stop your alarm from ringing",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { expanded = !expanded },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) { Text(if (expanded) "Hide" else "Fix") }
                }
            }
            if (!many || expanded) {
                warnings.forEachIndexed { index, w ->
                    if (many) Spacer(Modifier.height(if (index == 0) 4.dp else 12.dp))
                    Text(w.text, style = MaterialTheme.typography.bodyMedium)
                    TextButton(
                        onClick = w.onClick,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Text(w.buttonLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/** F004-R10..R13: alarm volume, gradual ramp, vibration and snooze length for one alarm. */
@Composable
private fun AlarmSoundSettings(
    alarm: Alarm,
    onVolumeChange: (Int) -> Unit,
    onGradualToggle: () -> Unit,
    onVibrationToggle: () -> Unit,
    onSnoozeSelect: (Int) -> Unit
) {
    // Local while dragging; saved (and the alarm re-armed) once, when the finger lifts.
    var volume by remember(alarm.volume) { mutableFloatStateOf(alarm.volume.toFloat()) }

    Column {
        Text("Alarm volume: ${volume.toInt()}%", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = volume,
            onValueChange = { volume = it },
            onValueChangeFinished = { onVolumeChange(volume.toInt()) },
            valueRange = 10f..100f,
            modifier = Modifier.semantics { contentDescription = "Alarm volume" }
        )
        SettingToggleRow(
            title = "Gradual volume",
            subtitle = "Starts quietly and reaches your volume in 30 seconds",
            checked = alarm.isGradualVolume,
            onToggle = onGradualToggle
        )
        SettingToggleRow(
            title = "Vibrate",
            subtitle = "Vibrates while the alarm rings",
            checked = alarm.isVibrationEnabled,
            onToggle = onVibrationToggle
        )
        Spacer(Modifier.height(8.dp))
        Text("Snooze length", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RingSettings.SNOOZE_CHOICES.forEach { minutes ->
                val selected = alarm.snoozeDurationMinutes == minutes
                if (selected) {
                    Button(
                        onClick = { onSnoozeSelect(minutes) },
                        modifier = Modifier.semantics { contentDescription = "Snooze $minutes minutes, selected" }
                    ) { Text("$minutes min") }
                } else {
                    OutlinedButton(
                        onClick = { onSnoozeSelect(minutes) },
                        modifier = Modifier.semantics { contentDescription = "Snooze $minutes minutes" }
                    ) { Text("$minutes min") }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/** A labelled switch whose whole row is the touch target (TalkBack reads it as one control). */
@Composable
private fun SettingToggleRow(title: String, subtitle: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = { onToggle() })
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
