package com.dailydivine.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    val x: Float,        // 0..1 across the width
    val startY: Float,   // starts above the top edge (negative)
    val speed: Float,    // how many screen-heights it falls over the animation
    val sway: Float,     // sideways wobble frequency
    val size: Float,     // px
    val rotation: Float,
    val spin: Float,
    val colorIndex: Int
)

/**
 * F003-R06 milestone celebration, 2000 ms (PRD animation table). The PRD
 * names a Lottie confetti; no Lottie asset is bundled (none could be fetched
 * offline), so this is drawn natively on a Canvas instead. It can be swapped
 * for LottieAnimation later without touching callers.
 */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier, durationMs: Int = 2000) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(durationMs, easing = LinearEasing)) }

    val primary = MaterialTheme.colorScheme.primary
    val palette = remember(primary) {
        listOf(Color(0xFFFFC107), Color(0xFFFFFFFF), primary, Color(0xFFFF8A65), Color(0xFF4DB6AC))
    }
    val particles = remember {
        val r = Random(42) // fixed seed: the same celebration every time
        List(70) {
            Particle(
                x = r.nextFloat(), startY = -r.nextFloat() * 0.3f, speed = 1.0f + r.nextFloat() * 0.7f,
                sway = 1f + r.nextFloat() * 2f, size = 10f + r.nextFloat() * 14f,
                rotation = r.nextFloat() * 360f, spin = 180f + r.nextFloat() * 540f,
                colorIndex = r.nextInt(palette.size)
            )
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        val fade = (1.2f - p).coerceIn(0f, 1f) // fade out over the last stretch
        particles.forEach { pt ->
            val x = (pt.x + sin(p * pt.sway * 2f * PI.toFloat()) * 0.03f) * size.width
            val y = (pt.startY + p * pt.speed) * size.height
            rotate(degrees = pt.rotation + p * pt.spin, pivot = Offset(x, y)) {
                drawRect(
                    color = palette[pt.colorIndex].copy(alpha = fade),
                    topLeft = Offset(x, y),
                    size = Size(pt.size, pt.size * 0.6f)
                )
            }
        }
    }
}
