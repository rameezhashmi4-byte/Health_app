package com.pushprime.model

/**
 * Quick Session templates (10-minute workouts).
 */
data class QuickSessionTemplate(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: QuickSessionDifficulty,
    val bestFor: String,
    val equipment: String,
    val rounds: Int,
    val workSeconds: Int,
    val restSeconds: Int,
    val exercises: List<String>
)

enum class QuickSessionDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

object QuickSessionTemplates {
    private const val DEFAULT_ROUNDS = 10
    private const val DEFAULT_WORK = 40
    private const val DEFAULT_REST = 20

    val all: List<QuickSessionTemplate> = listOf(
        QuickSessionTemplate(
            id = "fat_burner",
            name = "Fat Burner",
            description = "Fast-paced cardio burner",
            difficulty = QuickSessionDifficulty.HARD,
            bestFor = "Lose fat / stamina",
            equipment = "No equipment",
            rounds = DEFAULT_ROUNDS,
            workSeconds = DEFAULT_WORK,
            restSeconds = DEFAULT_REST,
            exercises = listOf(
                "Jumping jacks",
                "Squats",
                "Mountain climbers",
                "High knees",
                "Burpees (optional swap)"
            )
        ),
        QuickSessionTemplate(
            id = "upper_body_blast",
            name = "Upper Body Blast",
            description = "Push-focused strength",
            difficulty = QuickSessionDifficulty.MEDIUM,
            bestFor = "Build muscle",
            equipment = "Chair",
            rounds = DEFAULT_ROUNDS,
            workSeconds = DEFAULT_WORK,
            restSeconds = DEFAULT_REST,
            exercises = listOf(
                "Push-ups",
                "Plank shoulder taps",
                "Tricep dips (chair)",
                "Pike push-ups"
            )
        ),
        QuickSessionTemplate(
            id = "core_destroyer",
            name = "Core Destroyer",
            description = "Core strength and control",
            difficulty = QuickSessionDifficulty.MEDIUM,
            bestFor = "Core strength",
            equipment = "No equipment",
            rounds = DEFAULT_ROUNDS,
            workSeconds = DEFAULT_WORK,
            restSeconds = DEFAULT_REST,
            exercises = listOf(
                "Plank",
                "Bicycle crunch",
                "Leg raises",
                "Russian twists"
            )
        ),
        QuickSessionTemplate(
            id = "legs_glutes",
            name = "Legs + Glutes",
            description = "Lower body burn",
            difficulty = QuickSessionDifficulty.MEDIUM,
            bestFor = "Leg strength",
            equipment = "No equipment",
            rounds = DEFAULT_ROUNDS,
            workSeconds = DEFAULT_WORK,
            restSeconds = DEFAULT_REST,
            exercises = listOf(
                "Lunges",
                "Wall sit",
                "Squat pulses",
                "Calf raises"
            )
        ),
        QuickSessionTemplate(
            id = "pull_up_booster",
            name = "Pull-Up Booster",
            description = "Strength with a bar",
            difficulty = QuickSessionDifficulty.HARD,
            bestFor = "Strength",
            equipment = "Pull-up bar",
            rounds = DEFAULT_ROUNDS,
            workSeconds = DEFAULT_WORK,
            restSeconds = DEFAULT_REST,
            exercises = listOf(
                "Pull-ups (sub max)",
                "Dead hang",
                "Scap pulls",
                "Negatives"
            )
        ),
        QuickSessionTemplate(
            id = "mobility_recovery",
            name = "Mobility + Recovery",
            description = "Restore and reset",
            difficulty = QuickSessionDifficulty.EASY,
            bestFor = "Recovery",
            equipment = "No equipment",
            rounds = DEFAULT_ROUNDS,
            workSeconds = DEFAULT_WORK,
            restSeconds = DEFAULT_REST,
            exercises = listOf(
                "Hip mobility",
                "Hamstring stretch",
                "Thoracic rotations",
                "Breathing"
            )
        )
    )

    fun byId(templateId: String): QuickSessionTemplate? {
        return all.firstOrNull { it.id == templateId }
    }
}
