package com.dailydivine.app.ui.wallpaper

import com.dailydivine.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.dailydivine.app.ui.components.previewMaxHeight
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
@OptIn(ExperimentalLayoutApi::class) // FlowRow wraps the chips instead of overflowing at large fonts
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
            Text(stringResource(R.string.daily_wallpaper_2), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.wake_up_to_today_s),
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
                    modifier = Modifier.heightIn(max = previewMaxHeight()).aspectRatio(preview.width.toFloat() / preview.height).clip(RoundedCornerShape(20.dp))
                )
            } else {
                Box(Modifier.height(previewMaxHeight()), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.a_preview_will_appear_once),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SectionTitle("Apply to")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !state.useCustom,
                onClick = viewModel::useThemeBackground,
                label = { Text(stringResource(R.string.theme_colours)) }
            )
            if (state.hasCustomImage) {
                FilterChip(selected = state.useCustom, onClick = viewModel::usePhotoBackground, label = { Text(stringResource(R.string.my_photo)) })
            }
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = !state.busy
            ) { Text(if (state.hasCustomImage) "Choose another photo" else "Choose a photo") }
            if (state.hasCustomImage) TextButton(onClick = viewModel::removePhoto) { Text(stringResource(R.string.remove_photo)) }
        }

        SectionTitle("Verse position")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextPosition.values().forEach { position ->
                FilterChip(
                    selected = state.position == position,
                    onClick = { viewModel.setPosition(position) },
                    label = { Text(position.label) }
                )
            }
        }
        Text(stringResource(R.string.home_screens_are_busy_with),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (state.target != WallpaperTarget.OFF) {
            SectionTitle("Daily update")
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            ListItem(
                modifier = Modifier.clickable { showTimePicker = true },
                headlineContent = { Text(stringResource(R.string.update_time)) },
                supportingContent = { Text(formatTime12h(state.hour, state.minute)) },
                trailingContent = { Text(stringResource(R.string.change), color = MaterialTheme.colorScheme.primary) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            }
            // Diagnostics: shows whether the background job really ran (and who triggered it).
            describeLastCheck(state.lastAttemptMillis, state.lastAttemptSource, state.lastAttemptResult, System.currentTimeMillis())
                ?.let { line ->
                    Text(
                        "Last check: $line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            if (state.lastUpdatedEpochDay >= 0) {
                Text(
                    "Last updated ${LocalDate.ofEpochDay(state.lastUpdatedEpochDay).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::applyNow,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(if (state.busy) "Working\u2026" else "Update wallpaper now") }
            TextButton(
                onClick = viewModel::turnOffAndRestoreDefault,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text(stringResource(R.string.turn_off_and_reset_to_default_wallpaper)) }
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
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "For on-time daily updates, find DailyDivine in the battery list and choose \"Don't optimize\". " +
                            "Some phones pause background apps overnight, so the wallpaper may only change when you open the app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp), onClick = {
                        // The system list needs no permission (ACTION_REQUEST_IGNORE_... does, and Play restricts it).
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }) { Text(stringResource(R.string.open_battery_settings)) }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Good to know: DailyDivine replaces your wallpaper once a day and can't bring back the exact picture you had before. " +
                "\"Turn off and reset\" returns your phone's default wallpaper; choosing Off in the list above just stops the daily changes and leaves the last verse in place. " +
                "Some phones with their own theme or lock-screen apps may keep their own lock screen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    confirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmTarget = null },
            title = { Text(stringResource(R.string.replace_your_wallpaper)) },
            text = {
                Text(
                    "DailyDivine will set today's verse as your ${target.label.lowercase()} wallpaper and refresh it every day. " +
                        "Your current wallpaper will be replaced and can't be restored by the app."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setTarget(target); confirmTarget = null }) { Text(stringResource(R.string.replace)) }
            },
            dismissButton = { TextButton(onClick = { confirmTarget = null }) { Text(stringResource(R.string.cancel)) } }
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
