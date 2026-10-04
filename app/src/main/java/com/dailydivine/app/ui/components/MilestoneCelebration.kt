package com.dailydivine.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dailydivine.app.domain.model.MilestoneBadge

/** Full-screen celebration (PRD "Milestone Badge Screen" overlay): confetti,
 *  and the badge popping in with scale + bounce (PRD animation table). */
@Composable
fun MilestoneCelebration(
    badge: MilestoneBadge,
    onDismiss: () -> Unit,
    onViewBadges: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ConfettiOverlay()
            Card(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scale = remember { Animatable(0.3f) }
                    LaunchedEffect(Unit) {
                        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                    }
                    BadgeMedal(
                        earned = true,
                        size = 96.dp,
                        modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                    )
                    Spacer(Modifier.height(20.dp))
                    Text("Congratulations!", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (badge.days == 1) "You've reached 1 day" else "You've reached ${badge.days} days",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(badge.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onViewBadges, modifier = Modifier.fillMaxWidth()) { Text("View my badges") }
                    TextButton(onClick = onDismiss) { Text("Thank you") }
                }
            }
        }
    }
}
