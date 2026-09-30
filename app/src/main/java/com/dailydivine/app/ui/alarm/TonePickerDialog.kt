package com.dailydivine.app.ui.alarm

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailydivine.app.alarm.AlarmTones

/**
 * Tone selector (F004-R09) with tap-to-preview. Selecting a row previews it
 * once (not looping); "Select" commits. The preview player is released on
 * dismiss so it can never keep playing behind the dialog.
 */
@Composable
fun TonePickerDialog(
    currentToneId: String,
    religionId: Int?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(currentToneId) }
    val player = remember { arrayOfNulls<MediaPlayer>(1) }

    fun stopPreview() {
        try { player[0]?.stop() } catch (e: IllegalStateException) { /* not started */ }
        player[0]?.release()
        player[0] = null
    }

    fun preview(toneId: String) {
        stopPreview()
        try {
            val mp = MediaPlayer.create(
                context,
                AlarmTones.resIdFor(toneId),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            mp?.setOnCompletionListener { stopPreview() }
            mp?.start()
            player[0] = mp
        } catch (e: Exception) {
            player[0] = null // preview is a nicety; never crash the picker
        }
    }

    DisposableEffect(Unit) { onDispose { stopPreview() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alarm tone") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                items(AlarmTones.orderedFor(religionId), key = { it.id }) { tone ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { selected = tone.id; preview(tone.id) }
                    ) {
                        RadioButton(selected = selected == tone.id, onClick = { selected = tone.id; preview(tone.id) })
                        Text(tone.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { stopPreview(); onConfirm(selected) }) { Text("Select") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
