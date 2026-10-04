package com.dailydivine.app.ui.badges

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.domain.model.MilestoneBadge
import com.dailydivine.app.domain.model.StreakInfo
import com.dailydivine.app.ui.components.BadgeMedal

/** Streak & Badges screen (PRD screenshot list #7, F003, F009-R12 deep-link target). */
@Composable
fun BadgesScreen(onBack: () -> Unit, viewModel: BadgesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Streak & Badges", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        val streak = state.streak
        if (streak == null) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            StatsRow(streak)
            Spacer(Modifier.height(16.dp))
            ProgressCard(streak)
            Spacer(Modifier.height(24.dp))
            Text("Badges", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            // A badge is earned if the LONGEST streak ever reached it: breaking
            // a streak must not take back a badge already won.
            StreakInfo.MILESTONES.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { badge ->
                        BadgeTile(badge, earned = streak.longestStreak >= badge.days, modifier = Modifier.weight(1f))
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StatsRow(streak: StreakInfo) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile("Current", streak.currentStreak, Modifier.weight(1f))
        StatTile("Longest", streak.longestStreak, Modifier.weight(1f))
        StatTile("Total days", streak.totalDaysActive, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: Int, modifier: Modifier) {
    // PRD animation table: streak counter counts up over 800 ms.
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val shown by animateIntAsState(if (started) value else 0, tween(800), label = "count")

    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(shown.toString(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProgressCard(streak: StreakInfo) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(20.dp)) {
            val next = streak.nextMilestone
            Text(
                if (next != null) "${next.days - streak.currentStreak} days to \"${next.name}\"" else "Every badge earned. Remarkable.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = streak.progressToNext,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun BadgeTile(badge: MilestoneBadge, earned: Boolean, modifier: Modifier) {
    // PRD animation table: badge unlock = scale + bounce (earned badges only).
    val scale = remember { Animatable(if (earned) 0.6f else 1f) }
    LaunchedEffect(earned) {
        if (earned) scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = if (earned) 2.dp else 0.dp)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BadgeMedal(
                earned = earned,
                size = 64.dp,
                modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            )
            Spacer(Modifier.height(12.dp))
            Text(
                badge.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = if (earned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (earned) badge.description else "Reach ${badge.days} ${if (badge.days == 1) "day" else "days"}",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
