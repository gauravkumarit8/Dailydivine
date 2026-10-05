package com.dailydivine.app.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * A11Y-09: true when the user has turned animations off (Settings > Accessibility >
 * Remove animations, or Developer options > Animator duration scale = off).
 * Android reports that as an animator duration scale of 0. Decorative motion
 * (confetti, pulsing, bounces, count-ups) must be skipped, and the end state shown.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
