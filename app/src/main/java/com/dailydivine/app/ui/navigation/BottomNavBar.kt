package com.dailydivine.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

private data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Library.route, "Library", Icons.Filled.MenuBook),
    BottomNavItem(Screen.Alarm.route, "Alarm", Icons.Filled.Alarm),
    BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings)
)

/** Bottom nav bar for the four "returning user" destinations (PRD Section 8).
 *  Only ever shown for those four routes -- onboarding stays full-screen,
 *  see DailyDivineNavGraph's showBottomBar check. */
@Composable
fun DailyDivineBottomNavBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        BOTTOM_NAV_ITEMS.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Standard "switch tabs" pattern: pop back to the
                        // graph's start destination rather than piling up
                        // an ever-growing back stack across tab switches,
                        // but preserve each tab's own state/scroll position.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

/** Routes that should show the bottom nav bar -- everything else
 *  (onboarding, and any future full-screen overlay) hides it. */
fun isBottomNavRoute(route: String?): Boolean = route in BOTTOM_NAV_ITEMS.map { it.route }
