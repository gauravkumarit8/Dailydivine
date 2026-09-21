package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailydivine.app.util.ReligionMeta

/** Screen S04 (PRD Section 9): Onboarding 3/5 — languages filtered by the
 *  religion selected in S03 (F001-R05). */
@Composable
fun LanguageSelectScreen(
    religion: ReligionMeta?,
    selectedLanguageCode: String,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit
) {
    val languages = religion?.languages ?: listOf("English" to "en")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Select Your Language", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(languages) { (displayName, code) ->
                ListItem(
                    headlineContent = { Text(displayName) },
                    trailingContent = {
                        RadioButton(selected = selectedLanguageCode == code, onClick = { onSelect(code) })
                    },
                    modifier = Modifier.clickable { onSelect(code) }
                )
            }
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue →")
        }
    }
}
