package com.pushprime.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.pushprime.MainActivity
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Notification Helper
 * Smart daily exercise reminders with optimal timing
 */
class NotificationHelper(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val channelId = "workout_reminders"
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily exercise reminders"
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME_DAILY_REMINDER = "daily_exercise_reminder"
        
        // Optimal workout times (hours in 24-hour format)
        private val OPTIMAL_TIMES = listOf(
            7,  // Morning - 7 AM
            12, // Midday - 12 PM
            18  // Evening - 6 PM
        )
    }
    
    /**
     * Schedule daily exercise reminders
     * Automatically finds optimal time based on user's workout history
     */
    fun scheduleDailyReminders() {
        // Cancel existing work
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_DAILY_REMINDER)
        
        // Get optimal time based on user's typical workout times
        val optimalHour = getOptimalWorkoutTime()
        
        // Create periodic work request (daily)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val dailyWork = PeriodicWorkRequestBuilder<ExerciseReminderWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(optimalHour), TimeUnit.MILLISECONDS)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME_DAILY_REMINDER,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWork
            )
    }
    
    /**
     * Get optimal workout time based on user's history
     * Defaults to 7 AM if no history
     */
    private fun getOptimalWorkoutTime(): Int {
        val prefs = context.getSharedPreferences("PushPrimePrefs", Context.MODE_PRIVATE)
        val preferredHour = prefs.getInt("preferred_workout_hour", -1)
        
        if (preferredHour != -1) {
            return preferredHour
        }
        
        // Analyze workout times from database to find optimal time
        // For now, default to morning (7 AM)
        return OPTIMAL_TIMES[0]
    }
    
    /**
     * Calculate initial delay to next optimal workout time
     */
    private fun calculateInitialDelay(targetHour: Int): Long {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If target time has passed today, schedule for tomorrow
        if (currentHour >= targetHour) {
            targetCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return targetCalendar.timeInMillis - System.currentTimeMillis()
    }
    
    /**
     * Show notification immediately
     */
    fun showExerciseReminder(exerciseName: String = "workout") {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💪 Time for Your Workout!")
            .setContentText("Recommended: $exerciseName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time for your daily exercise! Recommended: $exerciseName"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancel all scheduled reminders
     */
    fun cancelReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_DAILY_REMINDER)
    }
    
    /**
     * Update preferred workout time based on user activity
     */
    fun updatePreferredWorkoutTime(hour: Int) {
        val prefs = context.getSharedPreferences("PushPrimePrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("preferred_workout_hour", hour).apply()
        // Reschedule with new time
        scheduleDailyReminders()
    }
}

/**
 * WorkManager Worker for daily exercise reminders
 */
class ExerciseReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Get recommended exercise for today
            val recommendedExercise = getRecommendedExercise()
            
            // Show notification
            NotificationHelper(applicationContext).showExerciseReminder(recommendedExercise)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun getRecommendedExercise(): String {
        // Get user's workout history to recommend exercise
        // For now, return a generic recommendation
        val exercises = listOf(
            "Push-ups",
            "Squats",
            "Plank",
            "Jumping Jacks",
            "Lunges"
        )
        return exercises.random()
    }
}
