package com.dailydivine.app.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/** F004-R07 time picker, factored out so onboarding (S05) and the Alarm
 *  list screen (S11) share one implementation rather than two copies that
 *  could silently drift apart.
 *
 *  Responsive: the dial is about 256 dp tall and does not fit short windows (landscape, split
 *  screen, very large fonts), so below 480 dp of window height it switches to keyboard entry
 *  (TimeInput), and the body scrolls as a last resort so OK/Cancel are never pushed out of reach.
 *  It follows the phone's 12/24-hour setting. */
@OptIn(ExperimentalMaterial3Api::class) // TimePicker is experimental in this BOM version
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val context = LocalContext.current
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = DateFormat.is24HourFormat(context)
    )
    val shortWindow = LocalConfiguration.current.screenHeightDp < 480
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (shortWindow) TimeInput(state = timePickerState) else TimePicker(state = timePickerState)
            }
        }
    )
}
