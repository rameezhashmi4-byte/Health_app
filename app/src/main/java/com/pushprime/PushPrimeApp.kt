package com.pushprime

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pushprime.data.AppDatabase
import com.pushprime.data.FirebaseHelper
import com.pushprime.data.LocalStore
import com.pushprime.network.VoipService
import com.pushprime.navigation.Screen
import com.pushprime.ui.components.BottomNavigationBar
import com.pushprime.ui.screens.*
import com.pushprime.ui.screens.ErrorScreen

/**
 * Main app composable with navigation
 * Modernized with bottom navigation and new screens
 */
@Composable
fun PushPrimeApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Initialize data stores - safe initialization for MVP
    val localStore = remember { 
        try {
            LocalStore(context)
        } catch (e: Exception) {
            null
        }
    }
    
    // Firebase and Voip are optional - app works without them
    val firebaseHelper = remember { 
        try {
            FirebaseHelper()
        } catch (e: Exception) {
            null
        }
    }
    val voipService = remember { 
        try {
            VoipService(context)
        } catch (e: Exception) {
            null
        }
    }
    
    // Initialize database
    val database = remember { AppDatabase.getDatabase(context) }
    val sessionDao = remember { database.sessionDao() }
    
    // Show error if LocalStore failed (critical component)
    if (localStore == null) {
        ErrorScreen(message = "Failed to initialize app storage")
        return
    }
    
    // Determine if we should show bottom nav (only on main tabs)
    val showBottomNav = currentRoute in listOf(
        Screen.Home.route,
        Screen.Workout.route,
        Screen.Progress.route,
        Screen.Compete.route,
        Screen.Profile.route
    )
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // ========== MAIN BOTTOM NAV SCREENS ==========
            
            composable(Screen.Home.route) {
                HomeScreen(
                    localStore = localStore,
                    onNavigateToWorkout = {
                        navController.navigate(Screen.Workout.route)
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
                    },
                    onNavigateToCompete = {
                        navController.navigate(Screen.Compete.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToPhotoVault = {
                        navController.navigate(Screen.PhotoVault.route)
                    },
                    onNavigateToSports = {
                        navController.navigate(Screen.SportsSelection.route)
                    },
                    onNavigateToTodayPlan = {
                        // TODO: Navigate to TodayPlanScreen when created
                        navController.navigate(Screen.Workout.route)
                    }
                )
            }
            
            composable(Screen.Workout.route) {
                WorkoutScreen(
                    onNavigateToWorkoutPlayer = { sessionId ->
                        navController.navigate(Screen.WorkoutPlayer.createRoute(sessionId))
                    },
                    onNavigateToSports = {
                        navController.navigate(Screen.SportsSelection.route)
                    },
                    onNavigateToExerciseLibrary = {
                        navController.navigate(Screen.ExerciseLibrary.route)
                    }
                )
            }
            
            composable(Screen.Progress.route) {
                ProgressScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onDayClick = { date ->
                        navController.navigate(Screen.CalendarDayDetail.createRoute(date))
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
            
            composable(Screen.Profile.route) {
                // TODO: Create ProfileScreen
                // For now, show a placeholder or redirect to Coaching
                CoachingScreen(
                    localStore = localStore,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // ========== NESTED ROUTES ==========
            
            composable(Screen.SportsSelection.route) {
                SportsSelectionScreen(
                    onSportSelected = { sport ->
                        // TODO: Start sport session
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.PhotoVault.route) {
                PhotoVaultScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCollageCreator = {
                        navController.navigate(Screen.CollageCreator.route)
                    }
                )
            }
            
            composable(Screen.CollageCreator.route) {
                // TODO: Create CollageCreatorScreen
                ErrorScreen(message = "Collage Creator coming soon!")
            }
            
            composable(
                route = Screen.CalendarDayDetail.route,
                arguments = listOf(
                    navArgument("date") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: ""
                // TODO: Create CalendarDayDetailScreen
                ErrorScreen(message = "Day detail for $date - Coming soon!")
            }
            
            // ========== LEGACY SCREENS (for backward compatibility) ==========
            
            composable(Screen.Dashboard.route) {
                // Redirect to Home for backward compatibility
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            }
            
            composable(Screen.Coaching.route) {
                CoachingScreen(
                    localStore = localStore,
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
}
