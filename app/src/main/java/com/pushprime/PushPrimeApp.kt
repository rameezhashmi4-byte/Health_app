package com.pushprime

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import com.pushprime.auth.AuthViewModel
import com.pushprime.auth.AuthViewModelFactory
import com.pushprime.data.AppDatabase
import com.pushprime.data.FirebaseHelper
import com.pushprime.data.LocalStore
import com.pushprime.navigation.Screen
import com.pushprime.network.VoipService
import com.pushprime.ui.components.BottomNavigationBar
import com.pushprime.ui.screens.*
import com.pushprime.ui.screens.ErrorScreen

/**
 * Main app composable with navigation
 * Modernized with bottom navigation and new screens
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PushPrimeApp() {
    val navController = rememberAnimatedNavController()
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
    
    // Initialize Spotify Helper
    val spotifyHelper = remember {
        try {
            com.pushprime.data.SpotifyHelper(context)
        } catch (e: Exception) {
            null
        }
    }
    
    // Show error if LocalStore failed (critical component)
    if (localStore == null) {
        ErrorScreen(message = "Failed to initialize app storage")
        return
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = remember(localStore) {
            AuthViewModelFactory(context.applicationContext, localStore)
        }
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val onboardingCompleted by localStore.onboardingCompleted.collectAsState()
    
    // Determine if we should show bottom nav (only on main tabs)
    // Use LaunchedEffect to ensure it updates reactively
    var showBottomNav by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentRoute) {
        val mainTabs = listOf(
            Screen.Home.route,
            Screen.Workout.route,
            Screen.Progress.route,
            Screen.Compete.route,
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
                        // Ensure navigation is always fluid - never block
                        // Use LaunchedEffect to prevent blocking the UI thread
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                // Always allow navigation between main tabs
                                val mainTabs = listOf(
                                    Screen.Home.route,
                                    Screen.Workout.route,
                                    Screen.Progress.route,
                                    Screen.Compete.route,
                                    Screen.Profile.route
                                )
                                
                                // If navigating to a main tab, clear back stack to that tab
                                if (route in mainTabs && currentRoute != route) {
                                    navController.navigate(route) {
                                        // Pop up to the start destination
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else if (route !in mainTabs) {
                                    // For nested routes, just navigate normally
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                    }
                                }
                            } catch (e: Exception) {
                                // If navigation fails, try simple navigation
                                try {
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                        }
                                    }
                                } catch (e2: Exception) {
                                    // Silently handle navigation errors - don't crash
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AnimatedNavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it / 6 }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it / 6 }
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it / 6 }
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it / 6 }
                )
            }
        ) {
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
                        launchSingleTop = true
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
                    onNavigateToSpotify = {
                        navController.navigate(Screen.SpotifyLogin.route)
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
                        localStore = localStore,
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
            
            composable(Screen.SpotifyLogin.route) {
                SpotifyLoginScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onConnectClick = {
                        spotifyHelper?.connect()
                        navController.navigate(Screen.SpotifyBrowser.route) {
                            popUpTo(Screen.SpotifyLogin.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.SpotifyBrowser.route) {
                SpotifyBrowserScreen(
                    spotifyHelper = spotifyHelper,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPlaylistSelected = { uri ->
                        // Playlist selected - can navigate back or stay
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
