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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    
    // Initialize database lazily (only when needed)
    val database = remember { 
        try {
            AppDatabase.getDatabase(context)
        } catch (e: Exception) {
            null
        }
    }
    
    // Show error if LocalStore failed (critical component)
    if (localStore == null) {
        ErrorScreen(message = "Failed to initialize app storage")
        return
    }
    
    // Determine if we should show bottom nav (only on main tabs)
    // Use remember to avoid recalculating on every recomposition
    val showBottomNav = remember(currentRoute) {
        currentRoute in listOf(
            Screen.Home.route,
            Screen.Workout.route,
            Screen.Progress.route,
            Screen.Compete.route,
            Screen.Profile.route
        )
    }
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        // Smooth navigation with animations
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
                        navController.navigate(Screen.TodayPlan.route) {
                            launchSingleTop = true
                        }
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
                if (database != null) {
                    ProgressScreen(
                        onNavigateBack = {}, // Main tab - no back needed
                        onDayClick = { date ->
                            navController.navigate(Screen.CalendarDayDetail.createRoute(date))
                        }
                    )
                } else {
                    ErrorScreen(message = "Database not available")
                }
            }
            
            composable(Screen.Compete.route) {
                CompeteScreen(
                    localStore = localStore,
                    firebaseHelper = firebaseHelper,
                    onNavigateBack = {} // Main tab - no back needed
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    localStore = localStore,
                    onNavigateToCoaching = {
                        navController.navigate(Screen.Coaching.route)
                    },
                    onNavigateToPhotoVault = {
                        navController.navigate(Screen.PhotoVault.route)
                    },
                    onNavigateBack = {} // Main tab - no back needed
                )
            }
            
            // ========== NESTED ROUTES ==========
            
            composable(
                route = Screen.WorkoutPlayer.route,
                arguments = listOf(
                    navArgument("sessionId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val sessionIdStr = backStackEntry.arguments?.getString("sessionId")
                val sessionId = sessionIdStr?.toLongOrNull()
                
                if (localStore != null) {
                    WorkoutPlayerScreen(
                        sessionId = sessionId,
                        localStore = localStore,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    ErrorScreen(
                        message = "Failed to initialize workout player",
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            
            composable(Screen.ExerciseLibrary.route) {
                ExerciseLibraryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onExerciseSelected = { exercise ->
                        // Navigate to workout player with exercise
                        navController.navigate(Screen.WorkoutPlayer.createRoute(null))
                    }
                )
            }
            
            composable(Screen.SportsSelection.route) {
                SportsSelectionScreen(
                    onSportSelected = { sport ->
                        // Start sport workout session
                        navController.navigate(Screen.WorkoutPlayer.createRoute(null)) {
                            popUpTo(Screen.SportsSelection.route) { inclusive = true }
                        }
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
                CollageCreatorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
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
                if (database != null) {
                    CalendarDayDetailScreen(
                        date = date,
                        sessionDao = database.sessionDao(),
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    ErrorScreen(
                        message = "Database not available",
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
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
            
            composable(Screen.TodayPlan.route) {
                TodayPlanScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onStartWorkout = {
                        navController.navigate(Screen.WorkoutPlayer.createRoute(null)) {
                            popUpTo(Screen.TodayPlan.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
