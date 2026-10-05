package com.dailydivine.app.ui.alarm

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
            if (!canScheduleExactAlarms) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Exact alarms are turned off for DailyDivine, so alarms may fire up to 10 minutes late. Enable them for reliable wake-up times.",
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

            if (!isIgnoringBatteryOptimizations) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Battery optimization may stop your alarm from firing in the background. Exempt DailyDivine for reliable wake-ups.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        }) { Text("Disable battery optimization") }
                    }
                }
            }

            if (alarms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No alarms yet — tap + to add your first morning blessing.",
                        modifier = Modifier.padding(32.dp)
                    )
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
    onDelete: () -> Unit
) {
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
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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
private fun DayToggle(letter: String, fullName: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) primary else primary.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .semantics { contentDescription = if (selected) "$fullName, repeats" else "$fullName, off" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
