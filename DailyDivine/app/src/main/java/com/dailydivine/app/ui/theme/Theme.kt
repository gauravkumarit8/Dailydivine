package com.dailydivine.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun DailyDivineTheme(
    religionId: Int = 0,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = paletteForReligionId(religionId)
    val colorScheme = if (darkTheme) {
        darkColorScheme(primary = palette.primary, secondary = palette.secondary)
    } else {
        lightColorScheme(primary = palette.primary, secondary = palette.secondary, surface = palette.surface)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DailyDivineTypography,
        content = content
    )
}
