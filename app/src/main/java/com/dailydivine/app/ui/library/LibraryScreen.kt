package com.dailydivine.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailydivine.app.data.local.entity.Verse
import com.dailydivine.app.domain.model.JournalHistoryItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.dailydivine.app.ui.theme.VerseTextStyle

/**
 * Screen S09 (PRD Section 9): Library. Browse (categories -> verses),
 * search across the user's religion, verse detail (S10) and Favorites.
 */
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val religion by viewModel.religion.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val query by viewModel.query.collectAsState()
    val verses by viewModel.verses.collectAsState()
    val bookmarked by viewModel.bookmarkedVerses.collectAsState()
    val detail by viewModel.detailVerse.collectAsState()
    val history by viewModel.history.collectAsState()
    val historyDetail by viewModel.historyDetail.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    val bookmarkedIds = remember(bookmarked) { bookmarked.map { it.id }.toSet() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
        Text("Library", style = MaterialTheme.typography.headlineLarge)
        religion?.let {
            Text(it.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Browse") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Favorites") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("History") })
        }
        Spacer(Modifier.height(16.dp))

        if (tab == 0) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search verses") },
                trailingIcon = {
                    if (query.isNotEmpty()) TextButton(onClick = { viewModel.setQuery("") }) { Text("Clear") }
                }
            )
            Spacer(Modifier.height(12.dp))

            when {
                query.isNotBlank() -> {
                    if (verses.isEmpty()) EmptyNote("No verses match \"${query.trim()}\".")
                    else VerseList(verses, onClick = viewModel::showDetail)
                }
                selectedCategory != null -> {
                    TextButton(onClick = viewModel::closeCategory) { Text("\u2190 All categories") }
                    Text(
                        "${selectedCategory!!.name} \u00B7 ${verses.size} verses",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    VerseList(verses, onClick = viewModel::showDetail)
                }
                categories.isEmpty() -> EmptyNote(
                    "Verses for ${religion?.name ?: "this path"} are coming soon. " +
                        "You can switch path in Settings."
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.openCategory(cat) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        } else if (tab == 1) {
            if (bookmarked.isEmpty()) {
                EmptyNote("No bookmarked verses yet \u2014 tap the bookmark icon on a verse to save it here.")
            } else {
                VerseList(bookmarked, onClick = viewModel::showDetail)
            }
        } else {
            if (history.isEmpty()) {
                EmptyNote("Your journey will appear here, one day at a time.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(history, key = { it.entry.id }) { item ->
                        HistoryRow(item, onClick = { viewModel.showHistoryDetail(item) })
                    }
                }
            }
        }
    }

    historyDetail?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::closeHistoryDetail,
            title = { Text(formatHistoryDate(item.entry.date), style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    item.verse?.let { v ->
                        Text("\u201C${v.translatedText}\u201D", style = VerseTextStyle)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "\u2014 ${v.sourceReference}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("YOUR REFLECTION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (item.entry.moodEmoji?.let { "$it  " } ?: "") +
                            (item.entry.journalText ?: "No reflection written for this day."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::closeHistoryDetail) { Text("Close") } }
        )
    }

    detail?.let { verse ->
        val isBookmarked = verse.id in bookmarkedIds
        AlertDialog(
            onDismissRequest = viewModel::closeDetail,
            title = { Text(verse.sourceReference, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("\u201C${verse.translatedText}\u201D", style = VerseTextStyle)
                }
            },
            confirmButton = { TextButton(onClick = viewModel::closeDetail) { Text("Close") } },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleBookmark(verse, isBookmarked) }) {
                    Icon(
                        if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark verse"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isBookmarked) "Saved" else "Save")
                }
            }
        )
    }
}

@Composable
private fun VerseList(verses: List<Verse>, onClick: (Verse) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(verses, key = { it.id }) { verse ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onClick(verse) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        verse.translatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        verse.sourceReference,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HistoryRow(item: JournalHistoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatHistoryDate(item.entry.date),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                item.entry.moodEmoji?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            }
            item.verse?.let {
                Spacer(Modifier.height(4.dp))
                Text(it.sourceReference, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                item.entry.journalText ?: "No reflection",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.entry.journalText == null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatHistoryDate(isoDate: String): String =
    try {
        LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        isoDate
    }
