package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailydivine.app.ui.theme.lightSchemePrimary
import com.dailydivine.app.ui.theme.paletteForReligionId
import com.dailydivine.app.util.ReligionMeta
import com.dailydivine.app.util.Religions

/** Screen S03 (PRD Section 9): Onboarding 2/5. */
@Composable
fun ReligionSelectScreen(
    selectedReligionId: Int?,
    onSelect: (Int) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScaffold(
        step = 2,
        title = "Choose Your Path",
        subtitle = "You can change this any time in Settings",
        actions = {
            Button(
                onClick = onContinue,
                enabled = selectedReligionId != null,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text("Continue \u2192") }
        }
    ) {
        // Rows of two (not LazyVerticalGrid): this content already scrolls, and a
        // lazy list inside a scrolling column has unbounded height and would crash.
        Religions.ALL.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
            ) {
                pair.forEach { religion ->
                    ReligionCard(
                        religion = religion,
                        selected = religion.id == selectedReligionId,
                        onClick = { onSelect(religion.id) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReligionCard(religion: ReligionMeta, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    // Each card shows its own religion's colour (dark enough for the white letter/check).
    val avatar = lightSchemePrimary(paletteForReligionId(religion.id))
    Card(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(avatar),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                } else {
                    Text(religion.name.first().toString(), style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(religion.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                religion.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
