package com.pushprime

import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pushprime.data.ExerciseRepository
import com.pushprime.data.FirebaseHelper
import com.pushprime.data.LocalStore
import com.pushprime.network.VoipService
import com.pushprime.navigation.Screen
import com.pushprime.ui.screens.*
import com.pushprime.ui.screens.ErrorScreen

/**
 * Main app composable with navigation
 */
@Composable
fun PushPrimeApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Initialize data stores - safe initialization for MVP
    val localStore = remember { 
        try {
            LocalStore(context)
        } catch (e: Exception) {
            // Fallback if LocalStore fails
            null
        }
    }
    
    // Firebase and Voip are optional - app works without them
    val firebaseHelper = remember { 
        try {
            FirebaseHelper()
        } catch (e: Exception) {
            null // Firebase not configured - app works in offline mode
        }
    }
    val voipService = remember { 
        try {
            VoipService(context)
        } catch (e: Exception) {
            null // Voip not configured - group sessions disabled
        }
    }
    
    // Show error if LocalStore failed (critical component)
    if (localStore == null) {
        ErrorScreen(message = "Failed to initialize app storage")
        return
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                localStore = localStore,
                onNavigateToCoaching = {
                    navController.navigate(Screen.Coaching.route)
                },
                onNavigateToCompete = {
                    navController.navigate(Screen.Compete.route)
                },
                onNavigateToGroup = {
                    navController.navigate(Screen.GroupSession.route)
                },
                onNavigateToMotivation = {
                    navController.navigate(Screen.Motivation.route)
                },
                onNavigateToMetrics = {
                    navController.navigate(Screen.Metrics.route)
                }
            )
        }
        
        composable(Screen.Coaching.route) {
            CoachingScreen(
                localStore = localStore,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Compete.route) {
            CompeteScreen(
                localStore = localStore,
                firebaseHelper = firebaseHelper, // Can be null - handled in screen
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.GroupSession.route) {
            if (voipService != null) {
                GroupSessionScreen(
                    voipService = voipService,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                ErrorScreen(message = "Group sessions require Twilio configuration")
            }
        }
        
        composable(Screen.Motivation.route) {
            MotivationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Metrics.route) {
            MetricsScreen(
                localStore = localStore,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
