package com.dailydivine.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailydivine.app.ui.home.HomeScreen
import com.dailydivine.app.ui.onboarding.*

@Composable
fun DailyDivineNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Onboarding flow (Section 9, S02-S06)
        composable(Screen.Welcome.route) {
            WelcomeScreen(onBegin = { navController.navigate(Screen.ReligionSelect.route) })
        }
        composable(Screen.ReligionSelect.route) {
            ReligionSelectScreen(
                onContinue = { navController.navigate(Screen.LanguageSelect.route) }
            )
        }
        composable(Screen.LanguageSelect.route) {
            LanguageSelectScreen(
                onContinue = { navController.navigate(Screen.AlarmSetup.route) }
            )
        }
        composable(Screen.AlarmSetup.route) {
            AlarmSetupScreen(
                onContinue = { navController.navigate(Screen.Permission.route) },
                onSkip = { navController.navigate(Screen.Permission.route) }
            )
        }
        composable(Screen.Permission.route) {
            PermissionScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // Main app
        composable(Screen.Home.route) { HomeScreen() }
        // Library / Alarm / Settings screens land in Sprint 3/4/7 per the
        // PRD timeline — routes reserved here so NavGraph doesn't need to
        // change shape when they're added.
    }
}
