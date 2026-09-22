package com.dailydivine.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.dailydivine.app.ui.alarm.AlarmListScreen
import com.dailydivine.app.ui.home.HomeScreen
import com.dailydivine.app.ui.library.LibraryScreen
import com.dailydivine.app.ui.onboarding.*
import com.dailydivine.app.ui.settings.SettingsScreen

@Composable
fun DailyDivineNavGraph(
    navController: NavHostController = rememberNavController(),
    // Root NavHost.startDestination MUST be a direct child of the root
    // graph. The onboarding screens (Welcome, ReligionSelect, ...) are
    // children of the NESTED "onboarding" sub-graph below, not of the root
    // -- so the correct default/entry point here is the sub-graph's own
    // route (Screen.OnboardingGraph.route = "onboarding"), which Navigation
    // then automatically descends into that sub-graph's own startDestination
    // (Screen.Welcome.route). Passing Screen.Welcome.route directly here
    // throws "navigation destination onboarding/welcome is not a direct
    // child of this NavGraph" on every launch -- see Screen.kt's comment
    // and CHECKLIST.md for the full story.
    startDestination: String = Screen.OnboardingGraph.route
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Bottom nav only appears for the four "returning user / main app"
    // destinations (PRD Section 8) -- onboarding stays full-screen.
    Scaffold(
        bottomBar = {
            if (isBottomNavRoute(currentRoute)) {
                DailyDivineBottomNavBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Onboarding flow (Section 9, S02-S06), nested so all five screens
            // share a single OnboardingViewModel instance scoped to this
            // sub-graph's own back stack entry -- selections made on one screen
            // (e.g. religion) are visible to later screens (e.g. language
            // filtering) without threading state through navigation arguments.
            navigation(startDestination = Screen.Welcome.route, route = Screen.OnboardingGraph.route) {
                composable(Screen.Welcome.route) {
                    WelcomeScreen(onBegin = { navController.navigate(Screen.ReligionSelect.route) })
                }
                composable(Screen.ReligionSelect.route) { entry ->
                    val viewModel = onboardingViewModel(navController, entry)
                    val state by viewModel.uiState.collectAsState()
                    ReligionSelectScreen(
                        selectedReligionId = state.selectedReligionId,
                        onSelect = viewModel::selectReligion,
                        onContinue = { navController.navigate(Screen.LanguageSelect.route) }
                    )
                }
                composable(Screen.LanguageSelect.route) { entry ->
                    val viewModel = onboardingViewModel(navController, entry)
                    val state by viewModel.uiState.collectAsState()
                    LanguageSelectScreen(
                        religion = state.selectedReligion,
                        selectedLanguageCode = state.selectedLanguageCode,
                        onSelect = viewModel::selectLanguage,
                        onContinue = { navController.navigate(Screen.AlarmSetup.route) }
                    )
                }
                composable(Screen.AlarmSetup.route) { entry ->
                    val viewModel = onboardingViewModel(navController, entry)
                    val state by viewModel.uiState.collectAsState()
                    AlarmSetupScreen(
                        alarmHour = state.alarmHour,
                        alarmMinute = state.alarmMinute,
                        onTimeChange = viewModel::setAlarmTime,
                        ttsEnabled = state.ttsEnabled,
                        onTtsToggle = viewModel::setTtsEnabled,
                        canScheduleExactAlarms = state.canScheduleExactAlarms,
                        onRequestExactAlarmPermission = viewModel::refreshExactAlarmPermission,
                        onContinue = {
                            viewModel.confirmAlarm()
                            navController.navigate(Screen.Permission.route)
                        },
                        onSkip = {
                            viewModel.skipAlarm()
                            navController.navigate(Screen.Permission.route)
                        }
                    )
                }
                composable(Screen.Permission.route) { entry ->
                    val viewModel = onboardingViewModel(navController, entry)
                    val state by viewModel.uiState.collectAsState()
                    PermissionScreen(
                        isCompleting = state.isCompleting,
                        onDone = { granted ->
                            viewModel.completeOnboarding(notificationsGranted = granted) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.OnboardingGraph.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }

            // Main app (bottom-nav destinations)
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
            composable(Screen.Alarm.route) { AlarmListScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onDataCleared = {
                        navController.navigate(Screen.OnboardingGraph.route) {
                            popUpTo(navController.graph.id) { inclusive = true } // clear the whole back stack, fresh start
                        }
                    }
                )
            }
        }
    }
}

/** Scopes OnboardingViewModel to the "onboarding" nested graph's own back
 *  stack entry, so every screen inside it shares the same instance. */
@Composable
private fun onboardingViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry
): OnboardingViewModel {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(Screen.OnboardingGraph.route)
    }
    return hiltViewModel(parentEntry)
}
