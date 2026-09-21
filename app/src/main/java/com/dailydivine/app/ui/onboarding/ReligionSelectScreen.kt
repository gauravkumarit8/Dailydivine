package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailydivine.app.util.Religions

/** Screen S03 (PRD Section 9): Onboarding 2/5 — 2-column grid, single-select. */
@OptIn(ExperimentalMaterial3Api::class) // clickable Card(onClick = ...) below is experimental in this BOM version
@Composable
fun ReligionSelectScreen(
    selectedReligionId: Int?,
    onSelect: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Choose Your Path", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f)) {
            items(Religions.ALL) { religion ->
                val isSelected = selectedReligionId == religion.id
                Card(
                    modifier = Modifier.padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { onSelect(religion.id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(religion.name, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(religion.tagline, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Button(
            onClick = onContinue,
            enabled = selectedReligionId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue →") }
    }
}
