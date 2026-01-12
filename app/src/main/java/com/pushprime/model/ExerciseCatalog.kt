package com.pushprime.model

/**
 * Exercise Catalog
 * Seed data for gym exercises organized by muscle group
 */
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val category: ExerciseCategory,
    val defaultMode: ExerciseMode,
    val equipment: Equipment,
    val difficulty: Difficulty,
    val shortInstructions: String,
    val shortTips: String
)

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    ARMS("Arms"),
    SHOULDERS("Shoulders"),
    CORE("Core"),
    LEGS("Legs"),
    FULL_BODY("Full Body"),
    CARDIO("Cardio")
}

enum class ExerciseCategory(val displayName: String) {
    BODYWEIGHT("Bodyweight"),
    FREE_WEIGHTS("Free Weights"),
    MACHINES("Machines")
}

enum class ExerciseMode(val displayName: String) {
    REPS("Reps"),
    TIMER("Timer"),
    HYBRID("Hybrid")
}

enum class Equipment(val displayName: String) {
    NONE("None"),
    DUMBBELLS("Dumbbells"),
    BARBELL("Barbell"),
    RESISTANCE_BANDS("Resistance Bands"),
    MACHINE("Machine"),
    CABLE("Cable")
}

enum class Difficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

/**
 * Exercise Catalog Seed Data
 */
object ExerciseCatalog {
    val exercises = listOf(
        // CHEST - Bodyweight
        Exercise(
            id = "push_ups",
            name = "Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Lower body until chest nearly touches floor, push back up",
            shortTips = "Keep core tight, full range of motion"
        ),
        Exercise(
            id = "diamond_push_ups",
            name = "Diamond Push-ups",
            muscleGroup = MuscleGroup.CHEST,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Hands form diamond shape, focus on triceps",
            shortTips = "More challenging, great for triceps"
        ),
        
        // CHEST - Free Weights
        Exercise(
            id = "bench_press",
            name = "Bench Press",
            muscleGroup = MuscleGroup.CHEST,
            category = ExerciseCategory.FREE_WEIGHTS,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.BARBELL,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Lower bar to chest, press up explosively",
            shortTips = "Control the descent, full lockout"
        ),
        
        // BACK - Bodyweight
        Exercise(
            id = "pull_ups",
            name = "Pull-ups",
            muscleGroup = MuscleGroup.BACK,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Pull body up until chin over bar, lower slowly",
            shortTips = "Full range of motion, engage lats"
        ),
        Exercise(
            id = "inverted_rows",
            name = "Inverted Rows",
            muscleGroup = MuscleGroup.BACK,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Pull chest to bar, body at angle",
            shortTips = "Great pull-up progression"
        ),
        
        // ARMS - Bodyweight
        Exercise(
            id = "dips",
            name = "Dips",
            muscleGroup = MuscleGroup.ARMS,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Lower body by bending arms, push back up",
            shortTips = "Focus on triceps, keep elbows in"
        ),
        
        // SHOULDERS - Bodyweight
        Exercise(
            id = "pike_push_ups",
            name = "Pike Push-ups",
            muscleGroup = MuscleGroup.SHOULDERS,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Handstand position, lower head to floor",
            shortTips = "Great shoulder builder"
        ),
        
        // CORE - Bodyweight
        Exercise(
            id = "plank",
            name = "Plank",
            muscleGroup = MuscleGroup.CORE,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.TIMER,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Hold straight line from head to heels",
            shortTips = "Engage core, breathe normally"
        ),
        Exercise(
            id = "sit_ups",
            name = "Sit-ups",
            muscleGroup = MuscleGroup.CORE,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Lift torso to knees, lower slowly",
            shortTips = "Control the movement"
        ),
        Exercise(
            id = "crunches",
            name = "Crunches",
            muscleGroup = MuscleGroup.CORE,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Lift shoulders off ground, squeeze abs",
            shortTips = "Less strain than sit-ups"
        ),
        
        // LEGS - Bodyweight
        Exercise(
            id = "squats",
            name = "Squats",
            muscleGroup = MuscleGroup.LEGS,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Lower until thighs parallel, drive up",
            shortTips = "Knees track over toes, chest up"
        ),
        Exercise(
            id = "lunges",
            name = "Lunges",
            muscleGroup = MuscleGroup.LEGS,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Step forward, lower back knee, push back",
            shortTips = "Keep front knee over ankle"
        ),
        Exercise(
            id = "jumping_jacks",
            name = "Jumping Jacks",
            muscleGroup = MuscleGroup.LEGS,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Jump feet wide, arms overhead, return",
            shortTips = "Great warm-up exercise"
        ),
        Exercise(
            id = "burpees",
            name = "Burpees",
            muscleGroup = MuscleGroup.LEGS,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Squat, jump back to plank, push-up, jump up",
            shortTips = "Full body exercise, high intensity"
        ),
        
        // FULL BODY - Bodyweight
        Exercise(
            id = "mountain_climbers",
            name = "Mountain Climbers",
            muscleGroup = MuscleGroup.FULL_BODY,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.INTERMEDIATE,
            shortInstructions = "Alternate bringing knees to chest rapidly",
            shortTips = "Keep core tight, fast pace"
        ),
        Exercise(
            id = "high_knees",
            name = "High Knees",
            muscleGroup = MuscleGroup.FULL_BODY,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.REPS,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Run in place, bring knees to chest",
            shortTips = "Great cardio warm-up"
        ),
        
        // CARDIO
        Exercise(
            id = "running",
            name = "Running",
            muscleGroup = MuscleGroup.CARDIO,
            category = ExerciseCategory.BODYWEIGHT,
            defaultMode = ExerciseMode.TIMER,
            equipment = Equipment.NONE,
            difficulty = Difficulty.BEGINNER,
            shortInstructions = "Maintain steady pace, proper form",
            shortTips = "Start slow, build endurance"
        )
    )
    
    fun getExercisesByMuscleGroup(group: MuscleGroup): List<Exercise> {
        return exercises.filter { it.muscleGroup == group }
    }
    
    fun getExercisesByCategory(category: ExerciseCategory): List<Exercise> {
        return exercises.filter { it.category == category }
    }
    
    fun getExerciseById(id: String): Exercise? {
        return exercises.find { it.id == id }
    }
}
