package com.pushprime

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.pushprime.auth.AuthViewModel
import com.pushprime.navigation.Screen
import com.pushprime.ui.components.BottomNavigationBar
import com.pushprime.ui.screens.*
import com.pushprime.ui.screens.nutrition.NutritionScreen
import com.pushprime.ui.screens.nutrition.NutritionViewModel

/**
 * Main app composable with navigation
 * Modernized with Hilt and standard Compose Navigation
 */
@Composable
fun PushPrimeApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Use Hilt-provided singleton if possible, but keep fallback for safety
    val localStore = remember { try { com.pushprime.di.DataModule.provideLocalStore(context) } catch (e: Exception) { null } }
    val firebaseHelper = remember { try { com.pushprime.data.FirebaseHelper() } catch (e: Exception) { null } }
    val database = remember { try { com.pushprime.data.AppDatabase.getDatabase(context) } catch (e: Exception) { null } }
    val spotifyHelper = remember { try { com.pushprime.data.SpotifyHelper(context) } catch (e: Exception) { null } }
    
    if (localStore == null) {
        ErrorScreen(message = "Failed to initialize app storage")
        return
    }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val onboardingCompleted by localStore.onboardingCompleted.collectAsState()
    
    var showBottomNav by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentRoute) {
        val mainTabs = listOf(
            Screen.Home.route,
            Screen.Workout.route,
            Screen.Progress.route,
            Screen.Profile.route
        )
        showBottomNav = currentRoute in mainTabs && !currentRoute.orEmpty().contains("/")
    }
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ... rest of the file ...
            composable(Screen.Splash.route) {
                var hasRouted by remember { mutableStateOf(false) }
                SplashScreen()
                LaunchedEffect(isLoggedIn, onboardingCompleted, hasRouted) {
                    if (hasRouted) return@LaunchedEffect
                    val target = when {
                        !onboardingCompleted -> Screen.Onboarding.route
                        isLoggedIn -> Screen.Home.route
                        else -> Screen.Auth.route
                    }
                    hasRouted = true
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onGetStarted = {
                        localStore.setOnboardingCompleted(true)
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                AuthScreen(
                    authViewModel = authViewModel,
                    onLoggedIn = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ========== MAIN BOTTOM NAV SCREENS ==========
            
            composable(Screen.Home.route) {
                HomeScreen(
                    localStore = localStore,
                    sessionDao = database?.sessionDao(),
                    spotifyHelper = spotifyHelper,
                    onNavigateToWorkout = {
                        navController.navigate(Screen.Workout.route)
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
                    },
                    onNavigateToNutrition = {
                        navController.navigate(Screen.Nutrition.route)
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
                    sessionDao = database?.sessionDao(),
                    onNavigateToPhotoVault = {
                        navController.navigate(Screen.PhotoVault.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateToAccount = {
                        navController.navigate(Screen.Account.route)
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
                        currentUserId = authViewModel.currentUser?.uid,
                        spotifyHelper = spotifyHelper,
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

            composable(Screen.Nutrition.route) {
                NutritionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    user = authViewModel.currentUser,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
