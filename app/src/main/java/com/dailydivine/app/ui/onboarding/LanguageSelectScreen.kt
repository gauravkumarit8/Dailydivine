package com.dailydivine.app.ui.onboarding

import com.dailydivine.app.R
import androidx.compose.ui.res.stringResource
import com.dailydivine.app.util.LanguageSupport
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dailydivine.app.util.ReligionMeta

/** Screen S04 (PRD Section 9): Onboarding 3/5. */
@Composable
fun LanguageSelectScreen(
    religion: ReligionMeta?,
    selectedLanguageCode: String,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit
) {
    val languages = religion?.languages ?: listOf("English" to "en")

    OnboardingScaffold(
        step = 3,
        title = "Select Your Language",
        subtitle = religion?.let { "For ${it.name}" },
        actions = {
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(stringResource(R.string.action_continue))
            }
        }
    ) {
        languages.forEach { (displayName, code) ->
            // One selectable row (radio is decorative inside it), so TalkBack reads
            // "English, selected, radio button" once instead of two controls.
            val available = LanguageSupport.isAvailable(religion?.id, code)
            ListItem(
                headlineContent = { Text(displayName) },
                supportingContent = if (!available) { { Text(stringResource(R.string.coming_soon)) } } else null,
                trailingContent = {
                    RadioButton(selected = selectedLanguageCode == code, onClick = null, enabled = available)
                },
                modifier = Modifier.selectable(
                    selected = selectedLanguageCode == code,
                    enabled = available,
                    role = Role.RadioButton,
                    onClick = { onSelect(code) }
                )
            )
        }
        if (languages.size > 1) {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.verses_are_currently_available_in),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
