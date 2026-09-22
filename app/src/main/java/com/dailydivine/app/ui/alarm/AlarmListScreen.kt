package com.dailydivine.app.ui.alarm

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.data.local.entity.Alarm
import com.dailydivine.app.ui.components.TimePickerDialog
import com.dailydivine.app.util.formatTime12h

/** Screen S11 (PRD Section 9): Alarm list + configuration. */
@Composable
fun AlarmListScreen(viewModel: AlarmListViewModel = hiltViewModel()) {
    val alarms by viewModel.alarms.collectAsState()
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addAlarm() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add alarm")
            }
        }
    ) { innerPadding ->
        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No alarms yet — tap + to add your first morning blessing.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        onToggle = { viewModel.toggleEnabled(alarm) },
                        onTimeClick = { editingAlarm = alarm },
                        onTtsToggle = { viewModel.toggleTts(alarm) },
                        onDelete = { viewModel.delete(alarm) }
                    )
                }
            }
        }
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
            Spacer(Modifier.height(8.dp))
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
