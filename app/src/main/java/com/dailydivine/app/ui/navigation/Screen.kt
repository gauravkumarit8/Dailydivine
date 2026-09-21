package com.dailydivine.app.ui.navigation

sealed class Screen(val route: String) {
    /** Route of the nested onboarding sub-graph itself (see NavGraph.kt) --
     *  this, not Welcome.route, is what the ROOT NavHost's startDestination
     *  must be. Welcome/ReligionSelect/etc. below are children of THIS
     *  graph, not direct children of the root graph -- passing one of them
     *  as the root NavHost's startDestination throws:
     *  "navigation destination onboarding/welcome is not a direct child of
     *  this NavGraph" immediately on every launch. */
    object OnboardingGraph : Screen("onboarding")

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
