package com.dailydivine.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/** Screen S07 (PRD Section 9): Home — greeting, daily verse card, reflection
 *  input, streak card, next-alarm banner. */
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            state.religion?.let { "Good Morning! — ${it.name}" } ?: "Good Morning!",
            style = MaterialTheme.typography.headlineMedium
        )
        state.streak?.let { streak ->
            Text("Day ${streak.totalDaysActive} of your journey", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            state.dailyVerse?.let { daily ->
                DailyVerseCard(
                    verseText = daily.verse.translatedText,
                    source = daily.verse.sourceReference,
                    isBookmarked = daily.isBookmarked
                )
            } ?: Text(
                "No verse content yet for ${state.religion?.name ?: "this religion"} — " +
                    "only Hinduism has sample content loaded in this build."
            )

            Spacer(Modifier.height(16.dp))
            state.streak?.let { StreakCard(it) }
        }
    }
}

@Composable
private fun DailyVerseCard(verseText: String, source: String, isBookmarked: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Today's Verse", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text("\"$verseText\"", fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text("— $source", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { /* F005: TTS play — Sprint 5 */ }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play verse aloud")
                }
                IconButton(onClick = { /* F002-R12: copy — Sprint 2 polish */ }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy verse text")
                }
                IconButton(onClick = { /* F007: bookmark toggle — Sprint 3 */ }) {
                    Icon(
                        if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark verse"
                    )
                }
                IconButton(onClick = { /* F008: share as image — Sprint 3 */ }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share verse")
                }
            }
        }
    }
}

@Composable
private fun StreakCard(streak: com.dailydivine.app.domain.model.StreakInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Streak: ${streak.currentStreak} days", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(8.dp))
            // NOTE: the lambda-based `progress = { ... }` overload of
            // LinearProgressIndicator was added in a newer Compose Material3
            // than the one pinned by our compose-bom (2024.01.00) resolves
            // to -- that version only has the plain `progress: Float`
            // overload. Using the lambda form fails with "None of the
            // following functions can be called with the arguments
            // supplied", listing only the Float overload as available.
            LinearProgressIndicator(
                progress = streak.progressToNext,
                modifier = Modifier.fillMaxWidth()
            )
            streak.nextMilestone?.let {
                Spacer(Modifier.height(4.dp))
                Text("Next: \"${it.name}\" at ${it.days} days", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
