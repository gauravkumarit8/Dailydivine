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

/** Round badge: religion-coloured gradient with a trophy when earned, grey with a lock when not. */
@Composable
fun BadgeMedal(earned: Boolean, size: Dp, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val brush = if (earned) {
        Brush.linearGradient(listOf(primary, lerp(primary, Color.Black, 0.4f)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E)))
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
