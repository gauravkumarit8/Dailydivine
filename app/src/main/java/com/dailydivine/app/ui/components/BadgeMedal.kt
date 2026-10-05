package com.dailydivine.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import com.dailydivine.app.ui.theme.darkenUntilContrast

/** Round badge: religion-coloured gradient with a trophy when earned, grey with a lock when not. */
@Composable
fun BadgeMedal(earned: Boolean, size: Dp, modifier: Modifier = Modifier) {
    // The icon is white, so the medal must be dark enough for white (4.5:1) in both themes.
    val base = darkenUntilContrast(MaterialTheme.colorScheme.primary, listOf(Color.White))
    val brush = if (earned) {
        Brush.linearGradient(listOf(base, lerp(base, Color.Black, 0.4f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF757575), Color(0xFF616161))) // white lock stays >= 4.5:1
    }
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(brush),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (earned) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
