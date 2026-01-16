package com.pushprime.navigation

sealed class Screen(val route: String) {
    // Auth flow
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Account : Screen("account")

    // Main Bottom Nav Tabs
    object Home : Screen("home")
    object Workout : Screen("workout")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    
    // Nested Routes
    object WorkoutPlayer : Screen("workout_player/{sessionId}") {
        fun createRoute(sessionId: Long? = null) = "workout_player/${sessionId ?: "new"}"
    }
    
    object SportsSelection : Screen("sports_selection")
    object ExerciseLibrary : Screen("exercise_library")
    
    // Progress Sub-screens
    object ProgressOverview : Screen("progress/overview")
    object ProgressCalendar : Screen("progress/calendar")
    object ProgressAnalytics : Screen("progress/analytics")
    object CalendarDayDetail : Screen("calendar_day/{date}") {
        fun createRoute(date: String) = "calendar_day/$date"
    }
    
    // Photo Vault
    object PhotoVault : Screen("photo_vault")
    object CollageCreator : Screen("collage_creator")
    
    // Today Plan
    object TodayPlan : Screen("today_plan")
    
    // Nutrition
    object Nutrition : Screen("nutrition")
    
    // Settings
    object NotificationSettings : Screen("notification_settings")
    
    // Social/Other
    object Compete : Screen("compete")
    object Metrics : Screen("metrics")
    
    // Dashboard (kept for backward compatibility, redirects to Home)
    object Dashboard : Screen("dashboard")
}
