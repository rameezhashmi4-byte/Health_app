package com.pushprime.model

data class GeneratedWorkoutPlan(
    val id: Long = 0,
    val title: String,
    val totalDurationMinutes: Int,
    val goal: WorkoutGoal,
    val timeMinutes: Int,
    val equipment: EquipmentOption,
    val focus: WorkoutFocus?,
    val style: TrainingStyle?,
    val blocks: List<WorkoutBlock>
)

data class WorkoutBlock(
    val type: WorkoutBlockType,
    val title: String,
    val durationMinutes: Int?,
    val exercises: List<GeneratedExercise>
)

data class GeneratedExercise(
    val name: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val seconds: Int? = null,
    val restSeconds: Int = 0,
    val notes: String? = null,
    val intensityTag: String? = null,
    val difficultyTag: String? = null,
    /**
     * Baseline calorie estimate for this exercise assuming a 70kg user.
     * Live session estimation should refine this based on actual time spent.
     */
    val caloriesEstimate: Int? = null
)

data class GeneratedWorkoutPlanSummary(
    val id: Long,
    val title: String,
    val totalDurationMinutes: Int,
    val goal: WorkoutGoal,
    val equipment: EquipmentOption,
    val focus: WorkoutFocus?
)

enum class WorkoutGoal(val displayName: String) {
    LOSE_FAT("Lose fat"),
    BUILD_MUSCLE("Build muscle"),
    STRENGTH("Get stronger"),
    STAMINA("Improve stamina")
}

enum class EquipmentOption(val displayName: String) {
    HOME("Home (no equipment)"),
    DUMBBELLS("Dumbbells"),
    FULL_GYM("Full Gym")
}

enum class WorkoutFocus(val displayName: String) {
    FULL_BODY("Full Body"),
    UPPER("Upper"),
    LOWER("Lower"),
    PULLUPS("Pull-ups focus"),
    CORE("Core")
}

enum class TrainingStyle(val displayName: String) {
    CIRCUIT("Circuit"),
    SUPERSETS("Supersets"),
    AMRAP("AMRAP"),
    EMOM("EMOM"),
    STRAIGHT_SETS("Straight Sets")
}

enum class WorkoutBlockType(val displayName: String) {
    WARMUP("Warm-up"),
    MAIN("Main Workout"),
    FINISHER("Finisher"),
    COOLDOWN("Cooldown")
}
