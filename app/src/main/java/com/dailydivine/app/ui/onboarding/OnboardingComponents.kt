package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dailydivine.app.ui.theme.darkenUntilContrast

/** PRD S02-S06: onboarding is 5 steps (Welcome, Path, Language, Alarm, Notifications). */
const val ONBOARDING_STEPS = 5

/**
 * Shared frame for every onboarding screen so they look like one flow: a step
 * indicator, a heading, a scrolling content area (so large font sizes or small
 * phones never push the buttons off-screen) and a pinned action area.
 */
@Composable
fun OnboardingScaffold(
    step: Int,
    title: String,
    subtitle: String? = null,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
        StepIndicator(step = step, total = ONBOARDING_STEPS)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            content = content
        )
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), content = actions)
    }
}

/** Pills, the current one wider. Read by TalkBack as one element: "Step 2 of 5". */
@Composable
fun StepIndicator(step: Int, total: Int) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = "Step $step of $total" }
    ) {
        repeat(total) { index ->
            val position = index + 1
            Box(
                Modifier
                    .height(6.dp)
                    .width(if (position == step) 28.dp else 12.dp)
                    .clip(CircleShape)
                    .background(if (position <= step) primary else primary.copy(alpha = 0.2f))
            )
        }
    }
}

/** Round gradient badge with a white icon; dark enough for white in either theme. */
@Composable
fun HeroMedal(icon: ImageVector, size: Dp, modifier: Modifier = Modifier) {
    val base = darkenUntilContrast(MaterialTheme.colorScheme.primary, listOf(Color.White))
    Box(
        modifier = modifier.size(size).clip(CircleShape)
            .background(Brush.linearGradient(listOf(base, lerp(base, Color.Black, 0.4f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }
}

/** Icon + title + one line of explanation, read by TalkBack as a single item. */
@Composable
fun FeatureRow(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
