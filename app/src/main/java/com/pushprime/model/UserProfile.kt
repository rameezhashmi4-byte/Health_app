package com.pushprime.model

enum class FitnessGoal {
    LOSE_FAT,
    BUILD_MUSCLE,
    GET_STRONGER,
    IMPROVE_STAMINA
}

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class SexOption {
    MALE,
    FEMALE,
    PREFER_NOT_TO_SAY
}

data class UserProfile(
    val uid: String = "",
    val fullName: String,
    val goal: FitnessGoal,
    val experience: ExperienceLevel,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int?,
    val sex: SexOption?,
    val stepTrackingEnabled: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun placeholder(uid: String, fallbackName: String): UserProfile {
            return UserProfile(
                uid = uid,
                fullName = fallbackName.ifBlank { "RAMBOOST User" },
                goal = FitnessGoal.GET_STRONGER,
                experience = ExperienceLevel.BEGINNER,
                weightKg = 0.0,
                heightCm = 0.0,
                age = null,
                sex = null,
                stepTrackingEnabled = false
            )
        }
    }
}
