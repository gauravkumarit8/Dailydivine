package com.dailydivine.app.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("onboarding/welcome")
    object ReligionSelect : Screen("onboarding/religion")
    object LanguageSelect : Screen("onboarding/language")
    object AlarmSetup : Screen("onboarding/alarm")
    object Permission : Screen("onboarding/permission")

    object Home : Screen("home")
    object Library : Screen("library")
    object Alarm : Screen("alarm")
    object Settings : Screen("settings")
}
