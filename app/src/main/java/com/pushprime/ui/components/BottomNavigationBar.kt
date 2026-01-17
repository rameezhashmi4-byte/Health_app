package com.pushprime.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pushprime.navigation.Screen

/**
 * Bottom Navigation Bar
 * Premium 4-tab navigation
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
            label = "Today",
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
            route = Screen.Profile.route,
            label = "Profile",
            icon = Icons.Default.Person
        )
    )
    
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                    )
                },
                label = {
                    if (isSelected) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
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
