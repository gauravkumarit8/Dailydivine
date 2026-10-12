package com.dailydivine.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Screen S02 (PRD Section 9): Onboarding 1/5. */
@Composable
fun WelcomeScreen(onBegin: () -> Unit) {
    OnboardingScaffold(
        step = 1,
        title = "Welcome to DailyDivine",
        subtitle = "A gentle daily companion for your faith",
        actions = {
            Button(onClick = onBegin, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Begin Your Journey")
            }
        }
    ) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            HeroMedal(Icons.Filled.AutoAwesome, size = 112.dp)
        }
        Spacer(Modifier.height(28.dp))
        FeatureRow(Icons.Filled.WbSunny, "A verse every day", "Wisdom from your own tradition, fresh each morning")
        FeatureRow(Icons.Filled.Alarm, "A peaceful alarm", "Wake to a temple bell, or a verse read aloud")
        FeatureRow(Icons.Filled.LocalFireDepartment, "Build your streak", "Small daily moments that add up")
    }
}
