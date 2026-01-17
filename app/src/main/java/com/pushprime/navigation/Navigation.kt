package com.pushprime.navigation

sealed class Screen(val route: String) {
    // Auth flow
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Account : Screen("account")
    object ProfileSetup : Screen("profile_setup")

    // Main Bottom Nav Tabs
    object Home : Screen("home")
    object Workout : Screen("workout")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object History : Screen("history")
    object StreakDetails : Screen("streak_details")

    object HistoryDetail : Screen("history_detail/{sessionId}?edit={edit}") {
        fun createRoute(sessionId: Long, edit: Boolean = false): String {
            return "history_detail/$sessionId?edit=$edit"
        }
    }
    
    // Nested Routes
    object WorkoutPlayer : Screen("workout_player/{sessionId}") {
        fun createRoute(sessionId: Long? = null) = "workout_player/${sessionId ?: "new"}"
    }

    object WorkoutGeneratorSetup : Screen("workout_generator/setup")
    object WorkoutGeneratorPreview : Screen("workout_generator/preview/{planId}") {
        fun createRoute(planId: Long) = "workout_generator/preview/$planId"
    }
    object WorkoutGeneratorSession : Screen("workout_generator/session/{planId}") {
        fun createRoute(planId: Long) = "workout_generator/session/$planId"
    }
    object WorkoutGeneratorSummary : Screen("workout_generator/summary/{planId}/{sessionId}") {
        fun createRoute(planId: Long, sessionId: Long) = "workout_generator/summary/$planId/$sessionId"
    }
    
    object SportsSelection : Screen("sports_selection")
    object ExerciseLibrary : Screen("exercise_library")

    object SportsModeSelector : Screen("sports_mode")
    object SportsSession : Screen("sports_session/{sportType}") {
        fun createRoute(sportType: String) = "sports_session/$sportType"
    }
    object SportsSummary : Screen(
        "sports_summary?sportType={sportType}&startTime={startTime}&endTime={endTime}" +
            "&durationSeconds={durationSeconds}&effort={effort}&intervals={intervals}&warmup={warmup}"
    ) {
        fun createRoute(
            sportType: String,
            startTime: Long,
            endTime: Long,
            durationSeconds: Int,
            effort: String,
            intervals: Boolean,
            warmup: Boolean
        ) = "sports_summary?sportType=$sportType&startTime=$startTime&endTime=$endTime" +
            "&durationSeconds=$durationSeconds&effort=$effort&intervals=$intervals&warmup=$warmup"
    }
    
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

    // Progress Photos
    object ProgressPhotos : Screen("progress_photos")
    object ProgressPhotoAdd : Screen("progress_photos/add")
    object ProgressPhotoCompare : Screen("progress_photos/compare?left={leftId}&right={rightId}") {
        fun createRoute(leftId: String, rightId: String): String {
            return "progress_photos/compare?left=$leftId&right=$rightId"
        }
    }
    object ProgressPhotoCollage : Screen("progress_photos/collage")
    object ProgressPhotoCollagePreview : Screen("progress_photos/collage/{collageId}") {
        fun createRoute(collageId: String): String {
            return "progress_photos/collage/$collageId"
        }
    }
    
    // Today Plan
    object TodayPlan : Screen("today_plan")
    
    // Session Mode
    object SessionMode : Screen("session_mode")

    // Quick Session
    object QuickSession : Screen("quick_session")
    object QuickSessionPlayer : Screen("quick_session_player/{templateId}") {
        fun createRoute(templateId: String) = "quick_session_player/$templateId"
    }
    object QuickSessionComplete : Screen("quick_session_complete/{templateId}?durationMinutes={durationMinutes}") {
        fun createRoute(templateId: String, durationMinutes: Int) =
            "quick_session_complete/$templateId?durationMinutes=$durationMinutes"
    }

    // Music Mode
    object MusicMode : Screen("music_mode")
    object SpotifyConnect : Screen("spotify_connect")
    object MusicSettings : Screen("music_settings")
    
    // Nutrition
    object Nutrition : Screen("nutrition")
    object NutritionAddMeal : Screen("nutrition/add_meal")
    object NutritionGoals : Screen("nutrition/goals")

    // Pull-Up Tracker
    object PullupTracker : Screen("pullup_tracker")
    object PullupLogSession : Screen("pullup_log_session")
    object PullupMaxTest : Screen("pullup_max_test")
    object PullupPlan : Screen("pullup_plan")

    // AI Coach
    object AiCoachChat : Screen("ai_coach_chat")
    object AiCoachSetup : Screen("ai_coach_setup")

    // Achievements
    object Achievements : Screen("achievements")
    
    // Settings
    object NotificationSettings : Screen("notification_settings")
    object VoiceCoachSettings : Screen("voice_coach_settings")
    
    // Social/Other
    object Compete : Screen("compete")
    object Metrics : Screen("metrics")
    object ShareProgress : Screen("share_progress")
    
    // Dashboard (kept for backward compatibility, redirects to Home)
    object Dashboard : Screen("dashboard")
}
