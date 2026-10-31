package com.dailydivine.app.ui.home

import com.dailydivine.app.util.StreakCopy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.dailydivine.app.util.VerseDisplayRules
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
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
    onOpenSettings: () -> Unit = {},
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
            .imePadding() // the reflection field must stay visible above the keyboard under edge-to-edge
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(greetingForNow(), style = MaterialTheme.typography.headlineLarge)
        // One quiet line (tradition, journey day) instead of two stacked lines, then the streak as a
        // compact chip: it used to be a full card at the very bottom that few people scrolled to.
        state.religion?.let { religion ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append(religion.name)
                    }
                    state.streak?.let { streak ->
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append("  \u00B7  Day ${streak.totalDaysActive} of your journey")
                        }
                    }
                },
                style = MaterialTheme.typography.titleSmall
            )
        }
        state.streak?.let { streak ->
            Spacer(Modifier.height(12.dp))
            StreakChip(streak, onClick = onOpenBadges)
        }
        Spacer(Modifier.height(20.dp))

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
            } ?: EmptyTraditionCard(
                name = state.religion?.name ?: "this path",
                onChooseAnother = onOpenSettings
            )

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
                // The chosen mood is named in words too, not only shown by a tint behind an emoji.
                mood?.let { m -> MOOD_EMOJIS.indexOf(m).takeIf { it >= 0 }?.let { "Feeling ${MOOD_LABELS[it]}" } }
                    ?: "How are you feeling?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        // A faint tint of the tradition's colour, so today's verse leads over the cards below it.
        colors = CardDefaults.cardColors(
            containerColor = lerp(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, 0.07f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "TODAY'S VERSE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            val verseSize = VerseDisplayRules.fontSizeSp(verseText.length)
            Text(
                "\u201C${VerseDisplayRules.normalize(verseText)}\u201D",
                style = VerseTextStyle.copy(fontSize = verseSize.sp, lineHeight = (verseSize * 1.6f).sp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "\u2014 $source",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            Spacer(Modifier.height(4.dp))
            // Labelled actions, spread evenly: icon-only buttons bunched on the left were easy to
            // miss and gave TalkBack nothing to read but an icon.
            Row(Modifier.fillMaxWidth()) {
                VerseAction(
                    icon = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    label = if (isSpeaking) "Stop" else "Listen",
                    onClick = onPlayToggle
                )
                VerseAction(icon = Icons.Filled.ContentCopy, label = "Copy", onClick = onCopy)
                VerseAction(
                    icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    label = if (isBookmarked) "Saved" else "Save",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    onClick = onBookmarkToggle
                )
                VerseAction(icon = Icons.Filled.Share, label = "Share", onClick = onShare)
            }
        }
    }
}

/** The streak as a compact, tappable chip (opens Streak & Badges). */
@Composable
private fun StreakChip(streak: com.dailydivine.app.domain.model.StreakInfo, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val content = MaterialTheme.colorScheme.onSecondaryContainer
    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(StreakCopy.title(streak.currentStreak), style = MaterialTheme.typography.titleSmall, color = content)
            streak.nextMilestone?.let {
                Text(
                    StreakCopy.next(it.name, it.days - streak.currentStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = content
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = content)
    }
}

@Composable
private fun RowScope.VerseAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = LocalContentColor.current
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 56.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

private fun greetingForNow(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 4..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..20 -> "Good Evening"
    else -> "Peaceful Night"
}

/** Shown only when the chosen tradition has no verses yet (e.g. an install from an older build). */
@Composable
private fun EmptyTraditionCard(name: String, onChooseAnother: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Verses for $name are coming soon",
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Until then, choose another tradition to begin your daily practice. You can switch back any time.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onChooseAnother, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Choose another tradition")
            }
        }
    }
}
