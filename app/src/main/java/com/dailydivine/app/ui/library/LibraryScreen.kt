package com.dailydivine.app.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Screen S09 (PRD Section 9): Library / Browse. Category grid, search, and
 * verse detail (S10) are Sprint 6 scope -- this placeholder exists so the
 * bottom nav tab has somewhere real to go rather than a missing route,
 * without pretending Sprint 6 is done.
 */
@Composable
fun LibraryScreen() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            "Library browsing is coming in a future update — check back soon!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
