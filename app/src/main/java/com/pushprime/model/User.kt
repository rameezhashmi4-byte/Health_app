package com.pushprime.model

/**
 * User model
 * Stores user profile for coaching and predictions
 */
data class User(
    val username: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.MALE,
    val fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    val predictedMaxPushups: Int = 0,
    val dailyGoal: Int = 0,
    val country: String = "US"
) {
    enum class Gender {
        MALE, FEMALE, OTHER
    }
    
    enum class FitnessLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
}
