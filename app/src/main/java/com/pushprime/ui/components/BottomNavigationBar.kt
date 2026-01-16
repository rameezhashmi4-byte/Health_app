package com.pushprime.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pushprime.navigation.Screen
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Bottom Navigation Bar
 * Instagram-like 5-tab navigation
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = Screen.Home.route,
            label = "Home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = Screen.Workout.route,
            label = "Workout",
            icon = Icons.Default.FitnessCenter
        ),
        BottomNavItem(
            route = Screen.Progress.route,
            label = "Progress",
            icon = Icons.Default.TrendingUp
        ),
        BottomNavItem(
            route = Screen.Compete.route,
            label = "Compete",
            icon = Icons.Default.EmojiEvents
        ),
        BottomNavItem(
            route = Screen.Profile.route,
            label = "Profile",
            icon = Icons.Default.Person
        )
    )
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

/**
 * Bottom Navigation Item
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
