package com.dailydivine.app.ui.alarm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
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
    onToggle: () -> Unit,
    onTimeClick: () -> Unit,
    onToneClick: () -> Unit,
    onTtsToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        formatTime12h(alarm.hour, alarm.minute),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(alarm.label, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onToneClick) {
                Text("Tone: ${com.dailydivine.app.alarm.AlarmTones.byId(alarm.alarmToneId).name}")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onTimeClick) { Text("Change time") }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("TTS", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = alarm.isTTSEnabled, onCheckedChange = { onTtsToggle() })
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete alarm")
                }
            }
        }
    }
}
