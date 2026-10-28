package com.dailydivine.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared sizing rules so every screen behaves on small phones, large phones, landscape,
 * tablets and foldables without each screen inventing its own numbers.
 */

/** Screens never stretch wider than this; on wider windows the content is centred. */
val MAX_CONTENT_WIDTH: Dp = 640.dp

/** From this window width (dp) the bottom bar is replaced by a navigation rail. */
const val WIDE_LAYOUT_MIN_DP = 600

/** Dialog bodies scroll within half the window height (never taller than 420 dp, never tiny). */
@Composable
fun dialogMaxHeight(): Dp {
    val h = (LocalConfiguration.current.screenHeightDp * 0.5f).dp
    return minOf(maxOf(h, 160.dp), 420.dp)
}

/** Wallpaper preview height: a share of the window, so it also fits short (landscape) windows. */
@Composable
fun previewMaxHeight(): Dp {
    val h = (LocalConfiguration.current.screenHeightDp * 0.55f).dp
    return minOf(maxOf(h, 220.dp), 420.dp)
}
