package com.dailydivine.app.ui.wallpaper

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.ui.components.TimePickerDialog
import com.dailydivine.app.util.formatTime12h
import com.dailydivine.app.wallpaper.TextPosition
import com.dailydivine.app.wallpaper.WallpaperTarget
import com.dailydivine.app.wallpaper.describeLastCheck
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Daily wallpaper: today's verse on your home and/or lock screen, on a theme gradient or your own photo. */
@Composable
fun WallpaperScreen(onBack: () -> Unit, viewModel: WallpaperViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var confirmTarget by remember { mutableStateOf<WallpaperTarget?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    // The system photo picker needs no storage permission; the app only ever sees the one photo chosen.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onImagePicked(uri)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Daily Wallpaper", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Wake up to today's verse on your screen. DailyDivine refreshes it once a day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        // Live preview of exactly what would be applied.
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val preview = state.preview
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "Preview of today's wallpaper with the daily verse",
                    modifier = Modifier.height(380.dp).aspectRatio(preview.width.toFloat() / preview.height).clip(RoundedCornerShape(20.dp))
                )
            } else {
                Box(Modifier.height(380.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "A preview will appear once a verse is available for your path.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SectionTitle("Apply to")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WallpaperTarget.values().forEach { target ->
                FilterChip(
                    selected = state.target == target,
                    onClick = {
                        // First time turning it on: ask, because this replaces their current wallpaper.
                        if (state.target == WallpaperTarget.OFF && target != WallpaperTarget.OFF) confirmTarget = target
                        else viewModel.setTarget(target)
                    },
                    // Short labels so four chips fit one row on a 360 dp phone; dialogs use the full label.
                    label = { Text(target.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        SectionTitle("Background")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !state.useCustom,
                onClick = viewModel::useThemeBackground,
                label = { Text("Theme colours") }
            )
            if (state.hasCustomImage) {
                FilterChip(selected = state.useCustom, onClick = viewModel::usePhotoBackground, label = { Text("My photo") })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = !state.busy
            ) { Text(if (state.hasCustomImage) "Choose another photo" else "Choose a photo") }
            if (state.hasCustomImage) TextButton(onClick = viewModel::removePhoto) { Text("Remove photo") }
        }

        SectionTitle("Verse position")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextPosition.values().forEach { position ->
                FilterChip(
                    selected = state.position == position,
                    onClick = { viewModel.setPosition(position) },
                    label = { Text(position.label) }
                )
            }
        }
        Text(
            "Home screens are busy with icons and lock screens show a clock near the top. Move the verse where it reads best.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (state.target != WallpaperTarget.OFF) {
            SectionTitle("Daily update")
            ListItem(
                modifier = Modifier.clickable { showTimePicker = true },
                headlineContent = { Text("Update time") },
                supportingContent = { Text(formatTime12h(state.hour, state.minute)) },
                trailingContent = { Text("Change", color = MaterialTheme.colorScheme.primary) }
            )
            // Diagnostics: shows whether the background job really ran (and who triggered it).
            describeLastCheck(state.lastAttemptMillis, state.lastAttemptSource, state.lastAttemptResult, System.currentTimeMillis())
                ?.let { line ->
                    Text(
                        "Last check: $line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            if (state.lastUpdatedEpochDay >= 0) {
                Text(
                    "Last updated ${LocalDate.ofEpochDay(state.lastUpdatedEpochDay).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::applyNow,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(if (state.busy) "Working\u2026" else "Update wallpaper now") }
        }

        // Battery: some phones stop background apps overnight, which is the main reason a daily
        // update can arrive late. Shown only while the feature is on and the app is restricted.
        val lifecycleOwner = LocalLifecycleOwner.current
        var ignoringBattery by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) ignoringBattery = isIgnoringBatteryOptimizations(context)
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        if (state.target != WallpaperTarget.OFF && !ignoringBattery) {
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "For on-time daily updates, find DailyDivine in the battery list and choose \"Don't optimize\". " +
                            "Some phones pause background apps overnight, so the wallpaper may only change when you open the app.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = {
                        // The system list needs no permission (ACTION_REQUEST_IGNORE_... does, and Play restricts it).
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }) { Text("Open battery settings") }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Good to know: DailyDivine replaces your wallpaper once a day and can't restore the one you had before. " +
                "Turn this off any time and the last wallpaper simply stays until you change it in your phone's settings. " +
                "Some phones with their own theme or lock-screen apps may keep their own lock screen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    confirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmTarget = null },
            title = { Text("Replace your wallpaper?") },
            text = {
                Text(
                    "DailyDivine will set today's verse as your ${target.label.lowercase()} wallpaper and refresh it every day. " +
                        "Your current wallpaper will be replaced and can't be restored by the app."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setTarget(target); confirmTarget = null }) { Text("Replace") }
            },
            dismissButton = { TextButton(onClick = { confirmTarget = null }) { Text("Cancel") } }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = state.hour,
            initialMinute = state.minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m -> viewModel.setTime(h, m) }
        )
    }
}

private fun isIgnoringBatteryOptimizations(context: android.content.Context): Boolean =
    (context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager)
        .isIgnoringBatteryOptimizations(context.packageName)

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(20.dp))
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
}
