package com.dailydivine.app.ui.gita

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.data.content.GitaChapter
import com.dailydivine.app.data.content.GitaPassage
import com.dailydivine.app.ui.theme.VerseTextStyle

private const val CREDITS =
    "Sanskrit: public domain. English: Annie Besant, The Bhagavad-Gita, 4th edition (1922), public domain; " +
        "transcription from English Wikisource, \"Bhagavad-Gita (Besant 4th)\", CC BY-SA 4.0. " +
        "\"In simple words\": written for DailyDivine."

/**
 * The whole Bhagavad Gita: 18 chapters, every verse in Sanskrit with Besant's English, and DailyDivine's own
 * plain-words meaning (English, or Hindi when Hindi is chosen in Settings) for the verses that have one.
 */
@Composable
fun GitaReaderScreen(onBack: () -> Unit, viewModel: GitaReaderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var chapterNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    val chapter = state.book.chapters.firstOrNull { it.number == chapterNumber }

    // Back from a chapter returns to the chapter list; back from the list leaves the reader.
    BackHandler(enabled = chapter != null) { chapterNumber = null }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (chapter != null) chapterNumber = null else onBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (chapter == null) "Bhagavad Gita" else "Chapter ${chapter.number}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
        }
        Spacer(Modifier.height(12.dp))
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.book.chapters.isEmpty() -> Text(
                "The Gita could not be loaded. Please reinstall the app, and let us know if it keeps happening.",
                style = MaterialTheme.typography.bodyLarge
            )
            chapter == null -> ChapterList(state.book.chapters, onOpen = { chapterNumber = it.number })
            else -> ChapterView(chapter, state.languageCode)
        }
    }
}

@Composable
private fun ChapterList(chapters: List<GitaChapter>, onOpen: (GitaChapter) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(chapters, key = { it.number }) { chapter ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { onOpen(chapter) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Chapter ${chapter.number}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(chapter.title, style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }
        item { Text(CREDITS, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun ChapterView(chapter: GitaChapter, languageCode: String) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(chapter.title, style = MaterialTheme.typography.titleLarge) }
        items(chapter.passages, key = { it.id }) { passage -> PassageCard(passage, languageCode) }
        item { Text(CREDITS, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun PassageCard(passage: GitaPassage, languageCode: String) {
    // Hindi readers see the Hindi plain-words meaning; everyone else the English one.
    val plainWords = if (languageCode == "hi") passage.meaningHi ?: passage.meaningEn else passage.meaningEn
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(passage.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            if (!passage.speaker.isNullOrBlank()) {
                Text(passage.speaker, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            }
            Spacer(Modifier.height(8.dp))
            Text(passage.sanskrit, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(passage.english, style = VerseTextStyle)
            if (!plainWords.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("In simple words", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(plainWords, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
