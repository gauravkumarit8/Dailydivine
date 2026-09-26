package com.dailydivine.app.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/** Screen S07 (PRD Section 9): Home — greeting, daily verse card, reflection
 *  input, streak card, next-alarm banner. */
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // No LaunchedEffect/load() call needed -- HomeViewModel reactively
    // observes UserPreferences in its own init block and loads
    // automatically, including re-loading if the religion changes in
    // Settings while this screen's ViewModel instance is retained.

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
                    isBookmarked = daily.isBookmarked,
                    isSpeaking = state.isSpeaking,
                    onPlayToggle = { viewModel.togglePlayVerse() },
                    onCopy = {
                        clipboardManager.setText(
                            AnnotatedString("${daily.verse.translatedText}\n— ${daily.verse.sourceReference}")
                        )
                    },
                    onBookmarkToggle = { viewModel.toggleBookmark() },
                    onShare = {
                        // F008-R02/R07: generate the image (off-main-thread,
                        // see HomeViewModel.generateShareImage) then hand it
                        // to Android's own share sheet -- we don't pick the
                        // destination app ourselves, ACTION_SEND + chooser
                        // is the correct, standard way to let the user do that.
                        coroutineScope.launch {
                            val uri = viewModel.generateShareImage()
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share verse"))
                            }
                        }
                    }
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
private fun DailyVerseCard(
    verseText: String,
    source: String,
    isBookmarked: Boolean,
    isSpeaking: Boolean,
    onPlayToggle: () -> Unit,
    onCopy: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Today's Verse", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text("\"$verseText\"", fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text("— $source", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = onPlayToggle) {
                    Icon(
                        if (isSpeaking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isSpeaking) "Stop reading" else "Play verse aloud"
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy verse text")
                }
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark verse"
                    )
                }
                IconButton(onClick = onShare) {
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
