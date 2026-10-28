package com.dailydivine.app.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
        // ORDER MATTERS. installSplashScreen() must run first: it switches the window from the splash
        // theme to the real theme. enableEdgeToEdge() builds the window decor, and when it ran first the
        // decor was built under the splash theme, which put an ActionBar (a grey "DailyDivine" title
        // bar) on every screen. WindowSetupTest guards this order.
        // Screen S01: keeps the branded splash up until MainViewModel has resolved whether onboarding
        // was already completed -- this prevents a flash of the Welcome screen for returning users.
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { mainViewModel.startDestination.value == null }
        // targetSdk 35+ enforces edge-to-edge (content draws behind the system bars). Opting in
        // explicitly keeps the status/navigation bar icons readable in light and dark themes;
        // the Scaffold in NavGraph applies the bar insets to the screens.
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        // F009-R12. savedInstanceState == null: only on a genuine launch from the
        // notification, not again after a rotation or process restore.
        val openBadgesOnStart = savedInstanceState == null &&
            intent?.getBooleanExtra(EXTRA_OPEN_BADGES, false) == true
        // F004-R18: launched from the alarm's "Wake Up & Read" with TTS on.
        val autoReadOnStart = savedInstanceState == null &&
            intent?.getBooleanExtra(EXTRA_AUTO_READ, false) == true
        setContent {
            val startDestination by mainViewModel.startDestination.collectAsState()
            val religionId by mainViewModel.currentReligionId.collectAsState()
            // BUG FIX: previously called with no religionId at all, so the
            // app always showed the default (Spiritual/teal) palette
            // regardless of what the user selected. See MainViewModel's
            // currentReligionId doc comment.
            DailyDivineTheme(religionId = religionId ?: 0) {
                // startDestination is guaranteed non-null by the time Compose
                // actually renders this, since the splash screen held above
                // blocks the very first frame until it resolves.
                startDestination?.let { destination ->
                    DailyDivineNavGraph(
                        startDestination = destination,
                        openBadgesOnStart = openBadgesOnStart,
                        autoReadOnStart = autoReadOnStart
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_BADGES = "open_badges"
        const val EXTRA_AUTO_READ = "auto_read"
    }
}
