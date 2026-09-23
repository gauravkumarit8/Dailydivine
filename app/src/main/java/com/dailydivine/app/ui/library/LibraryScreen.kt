package com.dailydivine.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen S09 (PRD Section 9): Library / Browse.
 *
 * Favorites (F007-R03) is real and backed by BookmarkRepository. Category
 * browsing, search, and verse detail (S10) are genuinely Sprint 6 scope and
 * not attempted here -- shown as an honest note below the Favorites list
 * rather than a silent missing feature.
 */
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val bookmarkedVerses by viewModel.bookmarkedVerses.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorites", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (bookmarkedVerses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "No bookmarked verses yet — tap the bookmark icon on a daily verse to save it here.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarkedVerses, key = { it.id }) { verse ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "\"${verse.translatedText}\"",
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("— ${verse.sourceReference}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Category browsing and search are coming in a future update.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
