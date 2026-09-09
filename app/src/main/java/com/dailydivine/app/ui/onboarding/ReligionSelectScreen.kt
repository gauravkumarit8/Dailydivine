package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Screen S03 (PRD Section 9): Onboarding 2/5 — 2-column grid, single-select. */
data class ReligionOption(val id: Int, val name: String, val tagline: String)

private val RELIGIONS = listOf(
    ReligionOption(1, "Hinduism", "Ancient wisdom of the Vedas"),
    ReligionOption(2, "Christianity", "Words of Christ & Scripture"),
    ReligionOption(3, "Islam", "Guidance from the Quran"),
    ReligionOption(4, "Buddhism", "Path to enlightenment"),
    ReligionOption(5, "Sikhism", "Teachings of the Gurus"),
    ReligionOption(6, "Judaism", "Torah & ancient wisdom"),
    ReligionOption(7, "Spiritual", "Universal wisdom & peace")
)

@Composable
fun ReligionSelectScreen(onContinue: (Int) -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Choose Your Path", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f)) {
            items(RELIGIONS) { religion ->
                val isSelected = selected == religion.id
                Card(
                    modifier = Modifier.padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { selected = religion.id }
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
            onClick = { selected?.let(onContinue) },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue →") }
    }
}
