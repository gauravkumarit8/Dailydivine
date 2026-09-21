package com.dailydivine.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dailydivine.app.ui.navigation.DailyDivineNavGraph
import com.dailydivine.app.ui.theme.DailyDivineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Screen S01: keeps the branded splash (Theme.DailyDivine.Splash) up
        // until MainViewModel has resolved whether onboarding was already
        // completed -- this is what prevents a flash of the Welcome screen
        // before jumping straight to Home for a returning user.
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { mainViewModel.startDestination.value == null }

        super.onCreate(savedInstanceState)
        setContent {
            val startDestination by mainViewModel.startDestination.collectAsState()
            DailyDivineTheme {
                // startDestination is guaranteed non-null by the time Compose
                // actually renders this, since the splash screen held above
                // blocks the very first frame until it resolves.
                startDestination?.let { destination ->
                    DailyDivineNavGraph(startDestination = destination)
                }
            }
        }
    }
}
