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
import com.pushprime.ui.components.AchievementPopupViewModel
import com.pushprime.ui.components.AchievementUnlockedPopup
import com.pushprime.ui.components.BottomNavigationBar
import com.pushprime.ui.screens.auth.*
import com.pushprime.ui.screens.common.ErrorScreen
import com.pushprime.ui.screens.home.HomeScreen
import com.pushprime.ui.screens.ProfileScreen
import com.pushprime.ui.screens.achievements.AchievementsScreen
import com.pushprime.ui.screens.social.CompeteScreen
import com.pushprime.ui.screens.progress.MetricsScreen
import com.pushprime.ui.screens.media.CollageCreatorScreen
import com.pushprime.ui.screens.media.PhotoVaultScreen
import com.pushprime.ui.screens.nutrition.AddMealScreen
import com.pushprime.ui.screens.nutrition.NutritionGoalsScreen
import com.pushprime.ui.screens.nutrition.NutritionScreen
import com.pushprime.ui.screens.pullup.PullupLogSessionScreen
import com.pushprime.ui.screens.pullup.PullupMaxTestScreen
import com.pushprime.ui.screens.pullup.PullupPlanScreen
import com.pushprime.ui.screens.pullup.PullupTrackerScreen
import com.pushprime.ui.screens.ai.AiCoachChatScreen
import com.pushprime.ui.screens.ai.AiCoachSetupScreen
import com.pushprime.ui.screens.profile_setup.ProfileSetupScreen
import com.pushprime.ui.screens.profile_setup.ProfileSetupViewModel
import com.pushprime.ui.screens.progress.CalendarDayDetailScreen
import com.pushprime.ui.screens.progress.ProgressScreen
import com.pushprime.ui.screens.quick_session.QuickSessionCompletionScreen
import com.pushprime.ui.screens.quick_session.QuickSessionPickerScreen
import com.pushprime.ui.screens.quick_session.QuickSessionPlayerScreen
import com.pushprime.ui.screens.share.ShareProgressScreen
import com.pushprime.ui.screens.settings.NotificationSettingsScreen
import com.pushprime.ui.screens.settings.MusicSettingsScreen
import com.pushprime.ui.screens.settings.VoiceCoachSettingsScreen
import com.pushprime.ui.screens.history.HistoryScreen
import com.pushprime.ui.screens.history.HistoryDetailScreen
import com.pushprime.ui.screens.streak.StreakDetailsScreen
import com.pushprime.model.Intensity
import com.pushprime.model.SportType
import com.pushprime.music.MusicSessionType
import com.pushprime.ui.screens.music.MusicModeScreen
import com.pushprime.ui.screens.music.SpotifyConnectScreen
import com.pushprime.ui.screens.sports.SportsModeSelectorScreen
import com.pushprime.ui.screens.sports.SportsSessionScreen
import com.pushprime.ui.screens.sports.SportsSessionSummaryScreen
import com.pushprime.ui.screens.sports.SportsSessionViewModel
import com.pushprime.ui.screens.workout.ExerciseLibraryScreen
import com.pushprime.ui.screens.workout.SportsSelectionScreen
import com.pushprime.ui.screens.workout.TodayPlanScreen
import com.pushprime.ui.screens.workout.SessionModeScreen
import com.pushprime.ui.screens.workout.WorkoutGeneratorPreviewScreen
import com.pushprime.ui.screens.workout.WorkoutGeneratorSetupScreen
import com.pushprime.ui.screens.workout.WorkoutGeneratorSummaryScreen
import com.pushprime.ui.screens.workout.WorkoutPlayerScreen
import com.pushprime.ui.screens.workout.WorkoutSessionPlayerScreen
import com.pushprime.ui.screens.workout.WorkoutScreen

/**
 * RAMBOOST app composable with navigation
 * Modernized with Hilt and standard Compose Navigation
 */
@Composable
fun RamboostApp(
    deepLinkRoute: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
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
    val isAuthLoading by authViewModel.isLoading.collectAsState()
    val onboardingCompleted by localStore.onboardingCompleted.collectAsState()
    val profileSetupCompleted by localStore.profileSetupCompleted.collectAsState()

    LaunchedEffect(isLoggedIn, onboardingCompleted) {
        if (isLoggedIn && !onboardingCompleted) {
            localStore.setOnboardingCompleted(true)
        }
    }
    
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
    
    val startDestination = remember(isLoggedIn, isAuthLoading, onboardingCompleted, profileSetupCompleted) {
        when {
            isAuthLoading -> Screen.Splash.route
            isLoggedIn && !profileSetupCompleted -> Screen.ProfileSetup.route
            isLoggedIn -> Screen.Home.route
            onboardingCompleted -> Screen.Auth.route
            else -> Screen.Onboarding.route
        }
    }

    LaunchedEffect(deepLinkRoute, isLoggedIn, currentRoute) {
        if (deepLinkRoute.isNullOrBlank()) return@LaunchedEffect
        if (!isLoggedIn) return@LaunchedEffect
        if (currentRoute == deepLinkRoute) {
            onDeepLinkConsumed()
            return@LaunchedEffect
        }
        navController.navigate(deepLinkRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        onDeepLinkConsumed()
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
        val popupViewModel: AchievementPopupViewModel = hiltViewModel()
        val popupAchievement by popupViewModel.popup.collectAsState()

        AuthGate(
            navController = navController,
            isLoggedIn = isLoggedIn,
            isLoading = isAuthLoading,
            onboardingCompleted = onboardingCompleted,
            profileSetupCompleted = profileSetupCompleted,
            currentRoute = currentRoute
        )

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ... rest of the file ...
            composable(Screen.Splash.route) {
                SplashScreen()
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
                        val target = if (localStore.profileSetupCompleted.value) {
                            Screen.Home.route
                        } else {
                            Screen.ProfileSetup.route
                        }
                        navController.navigate(target) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.ProfileSetup.route) {
                val setupViewModel: ProfileSetupViewModel = hiltViewModel()
                ProfileSetupScreen(
                    viewModel = setupViewModel,
                    onFinished = {
                        localStore.setProfileSetupCompleted(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ========== MAIN BOTTOM NAV SCREENS ==========
            
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToWorkout = { type ->
                        // Navigate to workout player with type or specific workout screen
                        navController.navigate(Screen.Workout.route)
                    },
                    onNavigateToSessionMode = {
                        navController.navigate(Screen.SessionMode.route)
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
                    },
                    onNavigateToSportsMode = {
                        navController.navigate(Screen.SportsModeSelector.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToQuickSession = {
                        navController.navigate(Screen.QuickSession.route)
                    },
                    onNavigateToProgressPhotos = {
                        navController.navigate(Screen.ProgressPhotos.route)
                    },
                    onNavigateToWorkoutGenerator = {
                        navController.navigate(Screen.WorkoutGeneratorSetup.route)
                    },
                    onNavigateToSavedPlan = { planId ->
                        navController.navigate(Screen.WorkoutGeneratorPreview.createRoute(planId))
                    },
                    onNavigateToStreakDetails = {
                        navController.navigate(Screen.StreakDetails.route)
                    },
                    onNavigateToMusicMode = {
                        navController.navigate(Screen.MusicMode.route)
                    },
                    onNavigateToNutrition = {
                        navController.navigate(Screen.Nutrition.route)
                    },
                    onNavigateToPullups = {
                        navController.navigate(Screen.PullupTracker.route)
                    },
                    onNavigateToAiCoach = {
                        navController.navigate(Screen.AiCoachChat.route)
                    }
                )
            }
            
            composable(Screen.Workout.route) {
                WorkoutScreen(
                    onNavigateToWorkoutPlayer = { sessionId ->
                        navController.navigate(Screen.WorkoutPlayer.createRoute(sessionId))
                    },
                    onNavigateToSports = {
                        navController.navigate(Screen.SportsModeSelector.route)
                    },
                    onNavigateToExerciseLibrary = {
                        navController.navigate(Screen.ExerciseLibrary.route)
                    },
                    onNavigateToWorkoutGenerator = {
                        navController.navigate(Screen.WorkoutGeneratorSetup.route)
                    }
                )
            }

            composable(Screen.WorkoutGeneratorSetup.route) {
                WorkoutGeneratorSetupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPreview = { planId ->
                        navController.navigate(Screen.WorkoutGeneratorPreview.createRoute(planId))
                    }
                )
            }

            composable(
                route = Screen.WorkoutGeneratorPreview.route,
                arguments = listOf(navArgument("planId") { type = NavType.LongType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
                WorkoutGeneratorPreviewScreen(
                    planId = planId,
                    onNavigateBack = { navController.popBackStack() },
                    onStartSession = {
                        navController.navigate(Screen.WorkoutGeneratorSession.createRoute(planId))
                    }
                )
            }

            composable(
                route = Screen.WorkoutGeneratorSession.route,
                arguments = listOf(navArgument("planId") { type = NavType.LongType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
                WorkoutSessionPlayerScreen(
                    planId = planId,
                    currentUserId = authViewModel.currentUser?.uid,
                    onNavigateBack = { navController.popBackStack() },
                    onFinishSession = { sessionId ->
                        navController.navigate(Screen.WorkoutGeneratorSummary.createRoute(planId, sessionId))
                    }
                )
            }

            composable(
                route = Screen.WorkoutGeneratorSummary.route,
                arguments = listOf(
                    navArgument("planId") { type = NavType.LongType },
                    navArgument("sessionId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getLong("planId") ?: 0L
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                WorkoutGeneratorSummaryScreen(
                    planId = planId,
                    sessionId = sessionId,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.SportsModeSelector.route) {
                SportsModeSelectorScreen(
                    onStartSession = { sport ->
                        navController.navigate(Screen.SportsSession.createRoute(sport.name))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SportsSession.route,
                arguments = listOf(navArgument("sportType") { type = NavType.StringType })
            ) { backStackEntry ->
                val sportType = backStackEntry.arguments?.getString("sportType")
                val selectedSport = try {
                    SportType.valueOf(sportType ?: SportType.FOOTBALL.name)
                } catch (_: Exception) {
                    SportType.FOOTBALL
                }
                SportsSessionScreen(
                    sportType = selectedSport,
                    localStore = localStore,
                    onFinish = { sport, startTime, endTime, durationSeconds, effort, intervals, warmup ->
                        navController.navigate(
                            Screen.SportsSummary.createRoute(
                                sportType = sport.name,
                                startTime = startTime,
                                endTime = endTime,
                                durationSeconds = durationSeconds,
                                effort = effort.name,
                                intervals = intervals,
                                warmup = warmup
                            )
                        )
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SportsSummary.route,
                arguments = listOf(
                    navArgument("sportType") { type = NavType.StringType },
                    navArgument("startTime") { type = NavType.LongType },
                    navArgument("endTime") { type = NavType.LongType },
                    navArgument("durationSeconds") { type = NavType.IntType },
                    navArgument("effort") { type = NavType.StringType },
                    navArgument("intervals") { type = NavType.BoolType },
                    navArgument("warmup") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val viewModel: SportsSessionViewModel = hiltViewModel()
                val sportType = backStackEntry.arguments?.getString("sportType")
                val effortName = backStackEntry.arguments?.getString("effort")
                val selectedSport = try {
                    SportType.valueOf(sportType ?: SportType.FOOTBALL.name)
                } catch (_: Exception) {
                    SportType.FOOTBALL
                }
                val effort = try {
                    Intensity.valueOf(effortName ?: Intensity.MEDIUM.name)
                } catch (_: Exception) {
                    Intensity.MEDIUM
                }
                val startTime = backStackEntry.arguments?.getLong("startTime") ?: 0L
                val endTime = backStackEntry.arguments?.getLong("endTime") ?: 0L
                val durationSeconds = backStackEntry.arguments?.getInt("durationSeconds") ?: 0
                val intervals = backStackEntry.arguments?.getBoolean("intervals") ?: false
                val warmup = backStackEntry.arguments?.getBoolean("warmup") ?: false

                SportsSessionSummaryScreen(
                    sportType = selectedSport,
                    startTime = startTime,
                    endTime = endTime,
                    durationSeconds = durationSeconds,
                    effortLevel = effort,
                    intervalsEnabled = intervals,
                    warmupEnabled = warmup,
                    onSave = { session ->
                        viewModel.saveSession(session) { result ->
                            if (result.isSuccess) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onDiscard = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
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

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenDetails = { sessionId, edit ->
                        navController.navigate(Screen.HistoryDetail.createRoute(sessionId, edit))
                    },
                    onStartSession = { navController.navigate(Screen.SessionMode.route) }
                )
            }

            composable(Screen.StreakDetails.route) {
                StreakDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQuickSession = { navController.navigate(Screen.QuickSession.route) }
                )
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
                    currentUserId = authViewModel.currentUser?.uid,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToShareProgress = {
                        navController.navigate(Screen.ShareProgress.route)
                    },
                    onNavigateToAchievements = {
                        navController.navigate(Screen.Achievements.route)
                    },
                    onNavigateToPhotoVault = {
                        navController.navigate(Screen.ProgressPhotos.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateToNutritionGoals = {
                        navController.navigate(Screen.NutritionGoals.route)
                    },
                    onNavigateToAiSetup = {
                        navController.navigate(Screen.AiCoachSetup.route)
                    },
                    onNavigateToVoiceCoachSettings = {
                        navController.navigate(Screen.VoiceCoachSettings.route)
                    },
                    onNavigateToCoachChat = {
                        navController.navigate(Screen.AiCoachChat.route)
                    },
                    onNavigateToAccount = {
                        navController.navigate(Screen.Account.route)
                    }
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
                    },
                    onNavigateToShareProgress = {
                        navController.navigate(Screen.ShareProgress.route)
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

            composable(
                route = Screen.HistoryDetail.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType },
                    navArgument("edit") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                val edit = backStackEntry.arguments?.getBoolean("edit") ?: false
                HistoryDetailScreen(
                    sessionId = sessionId,
                    startInEditMode = edit,
                    onNavigateBack = { navController.popBackStack() }
                )
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
                    },
                    onNavigateToAddMeal = {
                        navController.navigate(Screen.NutritionAddMeal.route)
                    }
                )
            }

            composable(Screen.NutritionAddMeal.route) {
                AddMealScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NutritionGoals.route) {
                NutritionGoalsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PullupTracker.route) {
                PullupTrackerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogSession = { navController.navigate(Screen.PullupLogSession.route) },
                    onTestMax = { navController.navigate(Screen.PullupMaxTest.route) },
                    onOpenPlan = { navController.navigate(Screen.PullupPlan.route) }
                )
            }

            composable(Screen.PullupLogSession.route) {
                PullupLogSessionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PullupMaxTest.route) {
                PullupMaxTestScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PullupPlan.route) {
                PullupPlanScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AiCoachChat.route) {
                AiCoachChatScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSetup = { navController.navigate(Screen.AiCoachSetup.route) }
                )
            }

            composable(Screen.AiCoachSetup.route) {
                AiCoachSetupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Achievements.route) {
                AchievementsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToShareProgress = {
                        navController.navigate(Screen.ShareProgress.route)
                    }
                )
            }

            composable(Screen.SessionMode.route) {
                SessionModeScreen(
                    localStore = localStore,
                    sessionDao = database?.sessionDao(),
                    currentUserId = authViewModel.currentUser?.uid,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.QuickSession.route) {
                QuickSessionPickerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartSession = { templateId ->
                        navController.navigate(Screen.QuickSessionPlayer.createRoute(templateId))
                    }
                )
            }

            composable(
                route = Screen.QuickSessionPlayer.route,
                arguments = listOf(
                    navArgument("templateId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId").orEmpty()
                val durationMinutes = com.pushprime.model.QuickSessionTemplates
                    .byId(templateId)
                    ?.let { (it.rounds * (it.workSeconds + it.restSeconds)) / 60 }
                    ?: 10
                QuickSessionPlayerScreen(
                    templateId = templateId,
                    onNavigateBack = { navController.popBackStack() },
                    onComplete = {
                        navController.navigate(
                            Screen.QuickSessionComplete.createRoute(templateId, durationMinutes)
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.QuickSessionComplete.route,
                arguments = listOf(
                    navArgument("templateId") { type = NavType.StringType },
                    navArgument("durationMinutes") { type = NavType.IntType; defaultValue = 10 }
                )
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId").orEmpty()
                val durationMinutes = backStackEntry.arguments?.getInt("durationMinutes") ?: 10
                QuickSessionCompletionScreen(
                    templateId = templateId,
                    durationMinutes = durationMinutes,
                    onSave = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.MusicMode.route) {
                MusicModeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartSession = { sessionType ->
                        when (sessionType) {
                            MusicSessionType.WORKOUT -> {
                                navController.navigate(Screen.WorkoutPlayer.createRoute(null))
                            }
                            MusicSessionType.SPORTS -> {
                                navController.navigate(Screen.SportsModeSelector.route)
                            }
                            MusicSessionType.QUICK_SESSION -> {
                                navController.navigate(Screen.QuickSession.route)
                            }
                        }
                    },
                    onConnectSpotify = {
                        navController.navigate(Screen.SpotifyConnect.route)
                    }
                )
            }

            composable(Screen.SpotifyConnect.route) {
                SpotifyConnectScreen(
                    spotifyHelper = spotifyHelper,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MusicSettings.route) {
                MusicSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.VoiceCoachSettings.route) {
                VoiceCoachSettingsScreen(
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
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        AchievementUnlockedPopup(achievement = popupAchievement)
    }
}

@Composable
private fun AuthGate(
    navController: androidx.navigation.NavHostController,
    isLoggedIn: Boolean,
    isLoading: Boolean,
    onboardingCompleted: Boolean,
    profileSetupCompleted: Boolean,
    currentRoute: String?
) {
    LaunchedEffect(isLoggedIn, isLoading, onboardingCompleted, profileSetupCompleted) {
        if (isLoading) return@LaunchedEffect
        val target = when {
            isLoggedIn && !profileSetupCompleted -> Screen.ProfileSetup.route
            isLoggedIn -> Screen.Home.route
            !onboardingCompleted -> Screen.Onboarding.route
            else -> Screen.Auth.route
        }
        if (currentRoute == target) return@LaunchedEffect
        navController.navigate(target) {
            when (target) {
                Screen.Home.route -> {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
                Screen.ProfileSetup.route -> {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
                Screen.Auth.route -> {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
                Screen.Onboarding.route -> {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
            launchSingleTop = true
        }
    }
}
