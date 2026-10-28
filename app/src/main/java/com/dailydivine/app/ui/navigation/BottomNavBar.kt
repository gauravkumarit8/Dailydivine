package com.dailydivine.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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

/** Standard "switch tabs" navigation: pop back to the graph's start destination rather than piling
 *  up an ever-growing back stack across tab switches, but keep each tab's own state/scroll position. */
private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Bottom nav bar for the four "returning user" destinations (PRD Section 8), used on narrow
 *  windows. Only ever shown for those four routes -- onboarding stays full-screen. */
@Composable
fun DailyDivineBottomNavBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        BOTTOM_NAV_ITEMS.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navigateToTab(navController, item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

/** The same four destinations as a side rail for wide windows (tablets, foldables, landscape),
 *  where a full-width bottom bar wastes height and looks stretched. */
@Composable
fun DailyDivineNavigationRail(navController: NavHostController, currentRoute: String?) {
    NavigationRail {
        BOTTOM_NAV_ITEMS.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = { navigateToTab(navController, item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

/** Routes that should show the navigation bar/rail -- everything else
 *  (onboarding, and any future full-screen overlay) hides it. */
fun isBottomNavRoute(route: String?): Boolean = route in BOTTOM_NAV_ITEMS.map { it.route }
