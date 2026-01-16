package com.pushprime.ui.screens

import androidx.compose.runtime.Composable
import com.pushprime.data.LocalStore

/**
 * Dashboard Screen (Deprecated)
 * Kept for backward compatibility only.
 */
@Composable
fun DashboardScreen(
    localStore: LocalStore,
    onNavigateToCoaching: () -> Unit,
    onNavigateToCompete: () -> Unit,
    onNavigateToGroup: () -> Unit,
    onNavigateToMotivation: () -> Unit,
    onNavigateToMetrics: () -> Unit = {},
    onNavigateToWorkout: () -> Unit = {}
) {
    ErrorScreen(message = "Dashboard has been replaced by Today.")
}
