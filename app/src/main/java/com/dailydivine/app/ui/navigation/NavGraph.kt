package com.dailydivine.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.dailydivine.app.ui.home.HomeScreen
import com.dailydivine.app.ui.onboarding.*

private const val ONBOARDING_GRAPH_ROUTE = "onboarding"

@Composable
fun DailyDivineNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Onboarding flow (Section 9, S02-S06), nested so all five screens
        // share a single OnboardingViewModel instance scoped to this
        // sub-graph's own back stack entry -- selections made on one screen
        // (e.g. religion) are visible to later screens (e.g. language
        // filtering) without threading state through navigation arguments.
        navigation(startDestination = Screen.Welcome.route, route = ONBOARDING_GRAPH_ROUTE) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(onBegin = { navController.navigate(Screen.ReligionSelect.route) })
            }
            composable(Screen.ReligionSelect.route) { backStackEntry ->
                val viewModel = onboardingViewModel(navController, backStackEntry)
                val state by viewModel.uiState.collectAsState()
                ReligionSelectScreen(
                    selectedReligionId = state.selectedReligionId,
                    onSelect = viewModel::selectReligion,
                    onContinue = { navController.navigate(Screen.LanguageSelect.route) }
                )
            }
            composable(Screen.LanguageSelect.route) { backStackEntry ->
                val viewModel = onboardingViewModel(navController, backStackEntry)
                val state by viewModel.uiState.collectAsState()
                LanguageSelectScreen(
                    religion = state.selectedReligion,
                    selectedLanguageCode = state.selectedLanguageCode,
                    onSelect = viewModel::selectLanguage,
                    onContinue = { navController.navigate(Screen.AlarmSetup.route) }
                )
            }
            composable(Screen.AlarmSetup.route) { backStackEntry ->
                val viewModel = onboardingViewModel(navController, backStackEntry)
                val state by viewModel.uiState.collectAsState()
                AlarmSetupScreen(
                    ttsEnabled = state.ttsEnabled,
                    onTtsToggle = viewModel::setTtsEnabled,
                    canScheduleExactAlarms = state.canScheduleExactAlarms,
                    onRequestExactAlarmPermission = viewModel::refreshExactAlarmPermission,
                    onContinue = { navController.navigate(Screen.Permission.route) },
                    onSkip = { navController.navigate(Screen.Permission.route) }
                )
            }
            composable(Screen.Permission.route) { backStackEntry ->
                val viewModel = onboardingViewModel(navController, backStackEntry)
                val state by viewModel.uiState.collectAsState()
                PermissionScreen(
                    isCompleting = state.isCompleting,
                    onDone = { granted ->
                        viewModel.completeOnboarding(notificationsGranted = granted) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(ONBOARDING_GRAPH_ROUTE) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        // Main app
        composable(Screen.Home.route) { HomeScreen() }
        // Library / Alarm / Settings screens land in Sprint 3/4/7 per the
        // PRD timeline — routes reserved here so NavGraph doesn't need to
        // change shape when they're added.
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
        navController.getBackStackEntry(ONBOARDING_GRAPH_ROUTE)
    }
    return hiltViewModel(parentEntry)
}
