package com.dailydivine.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Horizontal margin shared by every top-level screen (it was 16, 20 and 24 dp on different tabs). */
val ScreenHorizontalPadding = 20.dp

/**
 * The large title every tab starts with, so Home, Library, Alarm and Settings look like one app:
 * same margin, same type, optional coloured subtitle.
 */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = ScreenHorizontalPadding, end = ScreenHorizontalPadding, top = 24.dp, bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
