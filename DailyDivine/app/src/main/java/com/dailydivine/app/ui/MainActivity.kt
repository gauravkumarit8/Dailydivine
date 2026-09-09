package com.dailydivine.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dailydivine.app.ui.navigation.DailyDivineNavGraph
import com.dailydivine.app.ui.theme.DailyDivineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Screen S01: 1.5s branded splash, per Theme.DailyDivine.Splash
        super.onCreate(savedInstanceState)
        setContent {
            DailyDivineTheme {
                // TODO(Sprint 2 wiring): pass startDestination = Screen.Home.route
                // when UserPreferences already has onboardingCompleted = true.
                DailyDivineNavGraph()
            }
        }
    }
}
