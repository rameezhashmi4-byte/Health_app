package com.pushprime.ai

import com.pushprime.model.User

/**
 * Prediction Helper
 * Predicts push-up goals based on age, gender, and fitness level
 * Updates predictions weekly based on progress
 */
object PredictionHelper {
    /**
     * Predicts maximum push-ups based on user profile
     * 
     * Logic:
     * - Males: Beginner 20, Intermediate 35, Advanced 50
     * - Females: Beginner 12, Intermediate 25, Advanced 40
     * - Adjust down for age above 30
     */
    fun predictMaxPushups(user: User): Int {
        // Base predictions by gender and fitness level
        val basePrediction = when (user.gender) {
            User.Gender.MALE -> {
                when (user.fitnessLevel) {
                    User.FitnessLevel.BEGINNER -> 20
                    User.FitnessLevel.INTERMEDIATE -> 35
                    User.FitnessLevel.ADVANCED -> 50
                }
            }
            User.Gender.FEMALE -> {
                when (user.fitnessLevel) {
                    User.FitnessLevel.BEGINNER -> 12
                    User.FitnessLevel.INTERMEDIATE -> 25
                    User.FitnessLevel.ADVANCED -> 40
                }
            }
            User.Gender.OTHER -> {
                // Average of male and female
                when (user.fitnessLevel) {
                    User.FitnessLevel.BEGINNER -> 16
                    User.FitnessLevel.INTERMEDIATE -> 30
                    User.FitnessLevel.ADVANCED -> 45
                }
            }
        }
        
        // Age adjustment (reduce for age above 30)
        val ageAdjustment = when {
            user.age <= 30 -> 1.0f
            user.age <= 40 -> 0.95f
            user.age <= 50 -> 0.85f
            user.age <= 60 -> 0.75f
            else -> 0.65f
        }
        
        return (basePrediction * ageAdjustment).toInt()
    }
    
    /**
     * Updates prediction based on weekly progress
     * If user exceeds prediction, increase it
     * If user consistently below, adjust down slightly
     */
    fun updatePredictionBasedOnProgress(
        currentPrediction: Int,
        weeklyMax: Int,
        weeklyAverage: Int,
        weeksOfData: Int
    ): Int {
        if (weeksOfData == 0) return currentPrediction
        
        return when {
            // User exceeded prediction - increase it
            weeklyMax > currentPrediction -> {
                (currentPrediction * 1.1f).toInt().coerceAtMost(weeklyMax + 5)
            }
            // User consistently below - adjust down slightly
            weeklyAverage < currentPrediction * 0.7f && weeksOfData >= 2 -> {
                (currentPrediction * 0.9f).toInt().coerceAtLeast(weeklyAverage)
            }
            // User close to prediction - keep it
            else -> currentPrediction
        }
    }
    
    /**
     * Gets daily goal recommendation (70% of predicted max)
     */
    fun getDailyGoal(predictedMax: Int): Int {
        return (predictedMax * 0.7f).toInt()
    }
}
