package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Screen S02 (PRD Section 9): Onboarding 1/5, no skip option. */
@Composable
fun WelcomeScreen(onBegin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to DailyDivine", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "Start your day with divine wisdom",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onBegin, modifier = Modifier.fillMaxWidth()) {
            Text("Begin Your Journey →")
        }
    }
}
