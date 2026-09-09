package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Screen S04 (PRD Section 9): Onboarding 3/5 — languages filtered by the
 *  religion selected in S03 (F001-R05). */
@Composable
fun LanguageSelectScreen(
    availableLanguages: List<String> = listOf("English", "Hindi", "Sanskrit"),
    onContinue: (String) -> Unit
) {
    var selected by remember { mutableStateOf(availableLanguages.firstOrNull() ?: "English") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Select Your Language", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(availableLanguages) { lang ->
                ListItem(
                    headlineContent = { Text(lang) },
                    trailingContent = {
                        RadioButton(selected = selected == lang, onClick = { selected = lang })
                    },
                    modifier = Modifier.clickable { selected = lang }
                )
            }
        }
        Button(onClick = { onContinue(selected) }, modifier = Modifier.fillMaxWidth()) {
            Text("Continue →")
        }
    }
}
