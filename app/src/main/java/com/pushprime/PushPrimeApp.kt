package com.pushprime

import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pushprime.data.FirebaseHelper
import com.pushprime.data.LocalStore
import com.pushprime.network.VoipService
import com.pushprime.navigation.Screen
import com.pushprime.ui.screens.*

/**
 * Main app composable with navigation
 */
@Composable
fun PushPrimeApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Initialize data stores
    val localStore = remember { LocalStore(context) }
    val firebaseHelper = remember { FirebaseHelper() }
    val voipService = remember { VoipService(context) }
    
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
                firebaseHelper = firebaseHelper,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.GroupSession.route) {
            GroupSessionScreen(
                voipService = voipService,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Motivation.route) {
            MotivationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
