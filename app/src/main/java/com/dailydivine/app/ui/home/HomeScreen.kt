package com.dailydivine.app.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dailydivine.app.domain.model.MOOD_EMOJIS
import com.dailydivine.app.domain.model.MOOD_LABELS
import com.dailydivine.app.ui.components.MilestoneCelebration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.dailydivine.app.ui.theme.VerseTextStyle
import java.util.Calendar
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/** Screen S07 (PRD Section 9): Home — greeting, daily verse card, reflection
 *  input, streak card, next-alarm banner. */
@Composable
fun HomeScreen(
    onOpenBadges: () -> Unit = {},
    /** F004-R18: true when the app was opened via the alarm's "Wake Up & Read" with TTS on. */
    autoRead: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // HomeViewModel reactively observes UserPreferences in its own init block
    // (religion/install-date changes). On top of that, refresh whenever the
    // app returns to the foreground so a long-lived ViewModel never shows
    // yesterday's verse or misses recording today as opened.
    LaunchedEffect(state.ttsMessage) {
        state.ttsMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearTtsMessage()
        }
    }
    LaunchedEffect(autoRead, state.dailyVerse != null) {
        if (autoRead && state.dailyVerse != null) viewModel.autoRead()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.refresh()
                Lifecycle.Event.ON_STOP -> viewModel.flushReflection()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    state.milestoneReached?.let { badge ->
        MilestoneCelebration(
            badge = badge,
            onDismiss = viewModel::dismissMilestone,
            onViewBadges = {
                viewModel.dismissMilestone()
                onOpenBadges()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(greetingForNow(), style = MaterialTheme.typography.headlineLarge)
        state.religion?.let {
            Text(it.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        state.streak?.let { streak ->
            Text(
                "Day ${streak.totalDaysActive} of your journey",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))

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
                Spacer(Modifier.height(16.dp))
                ReflectionCard(
                    reflection = state.reflection,
                    mood = state.mood,
                    onReflectionChange = viewModel::onReflectionChanged,
                    onMoodSelect = viewModel::onMoodSelected
                )
            } ?: Text(
                "Verses for ${state.religion?.name ?: "this path"} are coming soon. " +
                    "You can switch path in Settings."
            )

            Spacer(Modifier.height(16.dp))
            state.streak?.let { StreakCard(it, onClick = onOpenBadges) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReflectionCard(
    reflection: String,
    mood: String?,
    onReflectionChange: (String) -> Unit,
    onMoodSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "YOUR REFLECTION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = reflection,
                onValueChange = onReflectionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("What does this verse mean to you today?") } // F011-R02
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "How are you feeling?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MOOD_EMOJIS.forEachIndexed { index, emoji ->
                    val selected = emoji == mood
                    val label = MOOD_LABELS[index]
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp) // A11Y-02, grows with font size
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .selectable(selected = selected, role = Role.RadioButton, onClick = { onMoodSelect(emoji) })
                            // A11Y-04: selection is announced, not only shown by the tint.
                            .semantics { contentDescription = "Feeling $label" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 24.sp)
                    }
                }
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "TODAY'S VERSE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("\u201C$verseText\u201D", style = VerseTextStyle)
            Spacer(Modifier.height(12.dp))
            Text(
                "\u2014 $source",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        contentDescription = "Bookmark verse",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
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
private fun StreakCard(streak: com.dailydivine.app.domain.model.StreakInfo, onClick: () -> Unit) {
    // Modifier.clickable rather than Card(onClick = ...): that overload is
    // experimental in this BOM and needs an @OptIn.
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Streak: ${streak.currentStreak} days", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = streak.progressToNext,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            streak.nextMilestone?.let {
                Spacer(Modifier.height(4.dp))
                Text("Next: \"${it.name}\" at ${it.days} days", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun greetingForNow(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 4..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..20 -> "Good Evening"
    else -> "Peaceful Night"
}
