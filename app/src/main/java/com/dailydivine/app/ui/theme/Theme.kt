package com.dailydivine.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DailyDivineTheme(
    religionId: Int = 0,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = paletteForReligionId(religionId)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            // BUG FIX: left at Material3's default before, which can pick a
            // poor-contrast color against our custom (non-default) primary/
            // secondary -- explicit white keeps button/FAB text and icons
            // reliably readable regardless of which religion's palette is
            // active.
            onPrimary = Color.White,
            onSecondary = Color.White
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            background = palette.surface,
            surface = palette.surface,
            surfaceVariant = Color.White,          // cards sit lighter than the tinted page
            surfaceContainerHighest = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DailyDivineTypography,
        // PRD 20.1: small 8dp (buttons), medium 12dp (cards), large 16dp (sheets)
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}
