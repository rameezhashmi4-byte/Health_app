package com.pushprime.data

import com.pushprime.model.EquipmentOption
import com.pushprime.model.GeneratedExercise
import com.pushprime.model.GeneratedWorkoutPlan
import com.pushprime.model.TrainingStyle
import com.pushprime.model.WorkoutBlock
import com.pushprime.model.WorkoutBlockType
import com.pushprime.model.WorkoutFocus
import com.pushprime.model.WorkoutGoal
import kotlin.random.Random

data class WorkoutGeneratorInputs(
    val goal: WorkoutGoal,
    val timeMinutes: Int,
    val equipment: EquipmentOption,
    val focus: WorkoutFocus?,
    val style: TrainingStyle?
)

class WorkoutGenerator {
    fun generate(
        inputs: WorkoutGeneratorInputs,
        avoidExerciseNames: Set<String> = emptySet()
    ): GeneratedWorkoutPlan {
        val random = Random(System.currentTimeMillis())
        val templatePlan = buildTemplatePlan(inputs, random, avoidExerciseNames)
        val plan = templatePlan ?: buildDynamicPlan(inputs, random, avoidExerciseNames)
        val signature = plan.blocks.flatMap { block -> block.exercises.map { it.name } }.toSet()
        if (avoidExerciseNames.isEmpty() || signature != avoidExerciseNames) {
            return plan
        }
        return buildDynamicPlan(inputs, random, avoidExerciseNames)
    }

    private fun buildTemplatePlan(
        inputs: WorkoutGeneratorInputs,
        random: Random,
        avoidExerciseNames: Set<String>
    ): GeneratedWorkoutPlan? {
        return when {
            inputs.goal == WorkoutGoal.LOSE_FAT &&
                inputs.timeMinutes == 30 &&
                inputs.equipment == EquipmentOption.FULL_GYM -> {
                val plan = GeneratedWorkoutPlan(
                    title = "Lose Fat Circuit - 30 min",
                    totalDurationMinutes = 30,
                    goal = inputs.goal,
                    timeMinutes = inputs.timeMinutes,
                    equipment = inputs.equipment,
                    focus = inputs.focus,
                    style = inputs.style ?: TrainingStyle.CIRCUIT,
                    blocks = listOf(
                        WorkoutBlock(
                            type = WorkoutBlockType.WARMUP,
                            title = "Warm-up",
                            durationMinutes = 5,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Rower",
                                    seconds = 180,
                                    restSeconds = 0,
                                    notes = "Easy pace",
                                    intensityTag = "Light"
                                ),
                                GeneratedExercise(
                                    name = "Dynamic stretches",
                                    seconds = 120,
                                    restSeconds = 0,
                                    notes = "Open hips, shoulders",
                                    intensityTag = "Light"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.MAIN,
                            title = "Main Circuit",
                            durationMinutes = 20,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Kettlebell swings",
                                    sets = 3,
                                    reps = 15,
                                    restSeconds = 30,
                                    notes = "Explosive hips",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Incline treadmill",
                                    seconds = 60,
                                    restSeconds = 30,
                                    notes = "Fast walk",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Push-ups",
                                    sets = 3,
                                    reps = 15,
                                    restSeconds = 30,
                                    notes = "Core tight",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Lat pulldown",
                                    sets = 3,
                                    reps = 12,
                                    restSeconds = 30,
                                    notes = "Full stretch",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Goblet squat",
                                    sets = 3,
                                    reps = 15,
                                    restSeconds = 30,
                                    notes = "Chest up",
                                    intensityTag = "High"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.FINISHER,
                            title = "Finisher",
                            durationMinutes = 4,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "HIIT intervals",
                                    seconds = 240,
                                    restSeconds = 0,
                                    notes = "20s on / 10s off",
                                    intensityTag = "Max"
                                )
                            )
                        )
                    )
                )
                adjustPlanForAvoid(plan, inputs, random, avoidExerciseNames)
            }
            inputs.goal == WorkoutGoal.BUILD_MUSCLE &&
                inputs.timeMinutes == 45 &&
                inputs.equipment == EquipmentOption.DUMBBELLS -> {
                val plan = GeneratedWorkoutPlan(
                    title = "Build Muscle - 45 min",
                    totalDurationMinutes = 45,
                    goal = inputs.goal,
                    timeMinutes = inputs.timeMinutes,
                    equipment = inputs.equipment,
                    focus = inputs.focus,
                    style = inputs.style ?: TrainingStyle.STRAIGHT_SETS,
                    blocks = listOf(
                        WorkoutBlock(
                            type = WorkoutBlockType.WARMUP,
                            title = "Warm-up",
                            durationMinutes = 5,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Mobility flow",
                                    seconds = 180,
                                    restSeconds = 0,
                                    notes = "Shoulders + hips",
                                    intensityTag = "Light"
                                ),
                                GeneratedExercise(
                                    name = "Light dumbbell presses",
                                    sets = 2,
                                    reps = 12,
                                    restSeconds = 30,
                                    notes = "Primer sets",
                                    intensityTag = "Light"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.MAIN,
                            title = "Hypertrophy",
                            durationMinutes = 34,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "DB bench press",
                                    sets = 4,
                                    reps = 10,
                                    restSeconds = 75,
                                    notes = "Full range",
                                    intensityTag = "Moderate"
                                ),
                                GeneratedExercise(
                                    name = "DB rows",
                                    sets = 4,
                                    reps = 10,
                                    restSeconds = 75,
                                    notes = "Pause at top",
                                    intensityTag = "Moderate"
                                ),
                                GeneratedExercise(
                                    name = "Shoulder press",
                                    sets = 3,
                                    reps = 10,
                                    restSeconds = 75,
                                    notes = "Control down",
                                    intensityTag = "Moderate"
                                ),
                                GeneratedExercise(
                                    name = "Lunges",
                                    sets = 3,
                                    reps = 12,
                                    restSeconds = 75,
                                    notes = "Even stride",
                                    intensityTag = "Moderate"
                                ),
                                GeneratedExercise(
                                    name = "Curls + triceps",
                                    sets = 3,
                                    reps = 12,
                                    restSeconds = 60,
                                    notes = "Superset",
                                    intensityTag = "Moderate"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.COOLDOWN,
                            title = "Cooldown",
                            durationMinutes = 6,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Stretch reset",
                                    seconds = 300,
                                    restSeconds = 0,
                                    notes = "Chest + back",
                                    intensityTag = "Light"
                                )
                            )
                        )
                    )
                )
                adjustPlanForAvoid(plan, inputs, random, avoidExerciseNames)
            }
            inputs.goal == WorkoutGoal.STRENGTH &&
                inputs.timeMinutes == 60 &&
                inputs.equipment == EquipmentOption.FULL_GYM -> {
                val mainLifts = when (inputs.focus) {
                    WorkoutFocus.UPPER -> listOf("Bench press", "Barbell row", "Overhead press")
                    WorkoutFocus.LOWER -> listOf("Back squat", "Deadlift", "Front squat")
                    else -> listOf("Back squat", "Deadlift", "Bench press")
                }
                val plan = GeneratedWorkoutPlan(
                    title = "Strength Block - 60 min",
                    totalDurationMinutes = 60,
                    goal = inputs.goal,
                    timeMinutes = inputs.timeMinutes,
                    equipment = inputs.equipment,
                    focus = inputs.focus,
                    style = inputs.style ?: TrainingStyle.STRAIGHT_SETS,
                    blocks = listOf(
                        WorkoutBlock(
                            type = WorkoutBlockType.WARMUP,
                            title = "Warm-up",
                            durationMinutes = 8,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Rower",
                                    seconds = 240,
                                    restSeconds = 0,
                                    notes = "Steady pace",
                                    intensityTag = "Light"
                                ),
                                GeneratedExercise(
                                    name = "Barbell warm-up sets",
                                    sets = 2,
                                    reps = 8,
                                    restSeconds = 45,
                                    notes = "Ramp weight",
                                    intensityTag = "Light"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.MAIN,
                            title = "Heavy Compounds",
                            durationMinutes = 40,
                            exercises = mainLifts.map { lift ->
                                GeneratedExercise(
                                    name = lift,
                                    sets = 5,
                                    reps = 5,
                                    restSeconds = 150,
                                    notes = "Heavy working sets",
                                    intensityTag = "Heavy"
                                )
                            }
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.FINISHER,
                            title = "Accessories",
                            durationMinutes = 8,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Pull-ups",
                                    sets = 3,
                                    reps = 6,
                                    restSeconds = 90,
                                    notes = "Strict reps",
                                    intensityTag = "Strong"
                                ),
                                GeneratedExercise(
                                    name = "Core circuit",
                                    seconds = 180,
                                    restSeconds = 30,
                                    notes = "Plank + hollow hold",
                                    intensityTag = "Moderate"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.COOLDOWN,
                            title = "Cooldown",
                            durationMinutes = 4,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Stretch reset",
                                    seconds = 240,
                                    restSeconds = 0,
                                    notes = "Lower back + hips",
                                    intensityTag = "Light"
                                )
                            )
                        )
                    )
                )
                adjustPlanForAvoid(plan, inputs, random, avoidExerciseNames)
            }
            inputs.goal == WorkoutGoal.STAMINA &&
                inputs.timeMinutes == 20 &&
                inputs.equipment == EquipmentOption.HOME -> {
                val plan = GeneratedWorkoutPlan(
                    title = "Stamina Sprint - 20 min",
                    totalDurationMinutes = 20,
                    goal = inputs.goal,
                    timeMinutes = inputs.timeMinutes,
                    equipment = inputs.equipment,
                    focus = inputs.focus,
                    style = inputs.style ?: TrainingStyle.AMRAP,
                    blocks = listOf(
                        WorkoutBlock(
                            type = WorkoutBlockType.WARMUP,
                            title = "Warm-up",
                            durationMinutes = 2,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Jumping jacks",
                                    seconds = 120,
                                    restSeconds = 0,
                                    notes = "Easy bounce",
                                    intensityTag = "Light"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.MAIN,
                            title = "AMRAP",
                            durationMinutes = 16,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Burpees",
                                    seconds = 40,
                                    restSeconds = 15,
                                    notes = "Fast pace",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Mountain climbers",
                                    seconds = 40,
                                    restSeconds = 15,
                                    notes = "Quick feet",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Air squats",
                                    seconds = 40,
                                    restSeconds = 15,
                                    notes = "Full depth",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Push-ups",
                                    seconds = 40,
                                    restSeconds = 15,
                                    notes = "Short sets",
                                    intensityTag = "High"
                                ),
                                GeneratedExercise(
                                    name = "Plank",
                                    seconds = 40,
                                    restSeconds = 15,
                                    notes = "Hold steady",
                                    intensityTag = "High"
                                )
                            )
                        ),
                        WorkoutBlock(
                            type = WorkoutBlockType.FINISHER,
                            title = "Finisher",
                            durationMinutes = 2,
                            exercises = listOf(
                                GeneratedExercise(
                                    name = "Sprint intervals (in place)",
                                    seconds = 120,
                                    restSeconds = 0,
                                    notes = "20s on / 10s off",
                                    intensityTag = "Max"
                                )
                            )
                        )
                    )
                )
                adjustPlanForAvoid(plan, inputs, random, avoidExerciseNames)
            }
            else -> null
        }
    }

    private fun buildDynamicPlan(
        inputs: WorkoutGeneratorInputs,
        random: Random,
        avoidExerciseNames: Set<String>
    ): GeneratedWorkoutPlan {
        val durations = durationProfile(inputs.timeMinutes)
        val goalProfile = goalProfile(inputs.goal)
        val pool = poolFor(inputs.equipment)
        val used = mutableSetOf<String>()
        val style = inputs.style

        val warmupBlock = WorkoutBlock(
            type = WorkoutBlockType.WARMUP,
            title = "Warm-up",
            durationMinutes = durations.warmupMinutes,
            exercises = buildWarmup(pool, inputs, random, avoidExerciseNames, used)
        )

        val mainBlock = WorkoutBlock(
            type = WorkoutBlockType.MAIN,
            title = mainBlockTitle(inputs),
            durationMinutes = durations.mainMinutes,
            exercises = buildMainExercises(pool, inputs, random, avoidExerciseNames, used, goalProfile)
        )

        val blocks = mutableListOf(warmupBlock, mainBlock)

        if (durations.finisherMinutes > 0) {
            blocks.add(
                WorkoutBlock(
                    type = WorkoutBlockType.FINISHER,
                    title = "Finisher",
                    durationMinutes = durations.finisherMinutes,
                    exercises = buildFinisher(pool, inputs, random, avoidExerciseNames, used, goalProfile)
                )
            )
        }

        if (durations.cooldownMinutes > 0) {
            blocks.add(
                WorkoutBlock(
                    type = WorkoutBlockType.COOLDOWN,
                    title = "Cooldown",
                    durationMinutes = durations.cooldownMinutes,
                    exercises = listOf(
                        GeneratedExercise(
                            name = "Stretch reset",
                            seconds = durations.cooldownMinutes * 60,
                            restSeconds = 0,
                            notes = "Slow breathing",
                            intensityTag = "Light",
                            difficultyTag = "Easy"
                        )
                    )
                )
            )
        }

        val title = buildTitle(inputs)
        return GeneratedWorkoutPlan(
            title = title,
            totalDurationMinutes = inputs.timeMinutes,
            goal = inputs.goal,
            timeMinutes = inputs.timeMinutes,
            equipment = inputs.equipment,
            focus = inputs.focus,
            style = style,
            blocks = blocks
        )
    }

    private fun adjustPlanForAvoid(
        plan: GeneratedWorkoutPlan,
        inputs: WorkoutGeneratorInputs,
        random: Random,
        avoidExerciseNames: Set<String>
    ): GeneratedWorkoutPlan {
        if (avoidExerciseNames.isEmpty()) return plan
        val pool = poolFor(inputs.equipment)
        val goalProfile = goalProfile(inputs.goal)
        val updatedBlocks = plan.blocks.map { block ->
            if (block.type != WorkoutBlockType.MAIN) return@map block
            val used = plan.blocks.flatMap { it.exercises.map { exercise -> exercise.name } }.toMutableSet()
            val updatedExercises = block.exercises.map { exercise ->
                if (!avoidExerciseNames.contains(exercise.name)) {
                    exercise
                } else {
                    val pattern = patternForName(exercise.name)
                    val option = pickExercise(
                        pool[pattern] ?: pool.getValue(ExercisePattern.FULL_BODY),
                        random,
                        avoidExerciseNames,
                        used
                    )
                    toGeneratedExercise(option, goalProfile, inputs.style, random)
                }
            }
            block.copy(exercises = updatedExercises)
        }
        return plan.copy(blocks = updatedBlocks)
    }

    private fun buildWarmup(
        pool: Map<ExercisePattern, List<ExerciseOption>>,
        inputs: WorkoutGeneratorInputs,
        random: Random,
        avoidExerciseNames: Set<String>,
        used: MutableSet<String>
    ): List<GeneratedExercise> {
        val warmupPool = pool[ExercisePattern.CARDIO].orEmpty() + pool[ExercisePattern.FULL_BODY].orEmpty()
        val warmupOptions = warmupPool.ifEmpty { pool.values.flatten() }
        val warmupCount = if (inputs.timeMinutes <= 20) 1 else 2
        return (0 until warmupCount).map {
            val option = pickExercise(warmupOptions, random, avoidExerciseNames, used)
            GeneratedExercise(
                name = option.name,
                seconds = 60 * if (warmupCount == 1) 2 else 2,
                restSeconds = 0,
                notes = "Warm up pace",
                intensityTag = "Light",
                difficultyTag = "Easy"
            )
        }
    }

    private fun buildMainExercises(
        pool: Map<ExercisePattern, List<ExerciseOption>>,
        inputs: WorkoutGeneratorInputs,
        random: Random,
        avoidExerciseNames: Set<String>,
        used: MutableSet<String>,
        goalProfile: GoalProfile
    ): List<GeneratedExercise> {
        val patternSequence = patternSequence(inputs.focus)
        val mainCount = when (inputs.timeMinutes) {
            10 -> 3
            20 -> 4
            30 -> 5
            45 -> 6
            else -> 6
        }
        val sequence = patternSequence.take(mainCount)
        return sequence.map { pattern ->
            val option = pickExercise(pool[pattern] ?: pool.getValue(ExercisePattern.FULL_BODY), random, avoidExerciseNames, used)
            toGeneratedExercise(option, goalProfile, inputs.style, random)
        }
    }

    private fun buildFinisher(
        pool: Map<ExercisePattern, List<ExerciseOption>>,
        inputs: WorkoutGeneratorInputs,
        random: Random,
        avoidExerciseNames: Set<String>,
        used: MutableSet<String>,
        goalProfile: GoalProfile
    ): List<GeneratedExercise> {
        val finisherOptions = pool[ExercisePattern.CARDIO].orEmpty() + pool[ExercisePattern.FULL_BODY].orEmpty()
        val option = pickExercise(finisherOptions.ifEmpty { pool.values.flatten() }, random, avoidExerciseNames, used)
        return listOf(
            GeneratedExercise(
                name = option.name,
                seconds = (inputs.timeMinutes.coerceAtLeast(20) / 5) * 60,
                restSeconds = 0,
                notes = "Finish strong",
                intensityTag = goalProfile.intensityTag,
                difficultyTag = "Hard"
            )
        )
    }

    private fun buildTitle(inputs: WorkoutGeneratorInputs): String {
        val focus = inputs.focus?.displayName ?: "Full Body"
        return "${inputs.goal.displayName} $focus - ${inputs.timeMinutes} min"
    }

    private fun mainBlockTitle(inputs: WorkoutGeneratorInputs): String {
        return when (inputs.style) {
            TrainingStyle.AMRAP -> "AMRAP"
            TrainingStyle.EMOM -> "EMOM"
            TrainingStyle.SUPERSETS -> "Superset Block"
            TrainingStyle.CIRCUIT -> "Circuit"
            TrainingStyle.STRAIGHT_SETS -> "Main Sets"
            null -> "Main Workout"
        }
    }

    private fun durationProfile(timeMinutes: Int): DurationProfile {
        return when (timeMinutes) {
            10 -> DurationProfile(warmupMinutes = 2, mainMinutes = 8, finisherMinutes = 0, cooldownMinutes = 0)
            20 -> DurationProfile(warmupMinutes = 3, mainMinutes = 14, finisherMinutes = 2, cooldownMinutes = 0)
            30 -> DurationProfile(warmupMinutes = 4, mainMinutes = 20, finisherMinutes = 4, cooldownMinutes = 2)
            45 -> DurationProfile(warmupMinutes = 5, mainMinutes = 30, finisherMinutes = 6, cooldownMinutes = 4)
            else -> DurationProfile(warmupMinutes = 6, mainMinutes = 40, finisherMinutes = 8, cooldownMinutes = 6)
        }
    }

    private fun goalProfile(goal: WorkoutGoal): GoalProfile {
        return when (goal) {
            WorkoutGoal.LOSE_FAT -> GoalProfile(12..20, 30..45, 30..45, "High")
            WorkoutGoal.BUILD_MUSCLE -> GoalProfile(8..12, 40..50, 60..90, "Moderate")
            WorkoutGoal.STRENGTH -> GoalProfile(3..6, 20..30, 120..180, "Heavy")
            WorkoutGoal.STAMINA -> GoalProfile(15..25, 30..60, 15..30, "Intervals", useTimedSets = true)
        }
    }

    private fun poolFor(equipment: EquipmentOption): Map<ExercisePattern, List<ExerciseOption>> {
        val fullBody = listOf(
            ExerciseOption("Jumping jacks", ExercisePattern.CARDIO, ExerciseMode.TIME),
            ExerciseOption("High knees", ExercisePattern.CARDIO, ExerciseMode.TIME),
            ExerciseOption("Burpees", ExercisePattern.FULL_BODY, ExerciseMode.TIME),
            ExerciseOption("Mountain climbers", ExercisePattern.CORE, ExerciseMode.TIME)
        )
        return when (equipment) {
            EquipmentOption.HOME -> mapOf(
                ExercisePattern.PUSH to listOf(
                    ExerciseOption("Push-ups", ExercisePattern.PUSH, ExerciseMode.REPS),
                    ExerciseOption("Pike push-ups", ExercisePattern.PUSH, ExerciseMode.REPS)
                ),
                ExercisePattern.PULL to listOf(
                    ExerciseOption("Towel rows", ExercisePattern.PULL, ExerciseMode.REPS),
                    ExerciseOption("Reverse snow angels", ExercisePattern.PULL, ExerciseMode.REPS)
                ),
                ExercisePattern.LEGS to listOf(
                    ExerciseOption("Air squats", ExercisePattern.LEGS, ExerciseMode.REPS),
                    ExerciseOption("Lunges", ExercisePattern.LEGS, ExerciseMode.REPS),
                    ExerciseOption("Glute bridges", ExercisePattern.LEGS, ExerciseMode.REPS)
                ),
                ExercisePattern.CORE to listOf(
                    ExerciseOption("Plank", ExercisePattern.CORE, ExerciseMode.TIME),
                    ExerciseOption("Dead bug", ExercisePattern.CORE, ExerciseMode.REPS),
                    ExerciseOption("Side plank", ExercisePattern.CORE, ExerciseMode.TIME)
                ),
                ExercisePattern.CARDIO to listOf(
                    ExerciseOption("High knees", ExercisePattern.CARDIO, ExerciseMode.TIME),
                    ExerciseOption("Jumping jacks", ExercisePattern.CARDIO, ExerciseMode.TIME),
                    ExerciseOption("Burpees", ExercisePattern.CARDIO, ExerciseMode.TIME)
                ),
                ExercisePattern.FULL_BODY to fullBody
            )
            EquipmentOption.DUMBBELLS -> mapOf(
                ExercisePattern.PUSH to listOf(
                    ExerciseOption("DB bench press", ExercisePattern.PUSH, ExerciseMode.REPS),
                    ExerciseOption("DB shoulder press", ExercisePattern.PUSH, ExerciseMode.REPS)
                ),
                ExercisePattern.PULL to listOf(
                    ExerciseOption("DB rows", ExercisePattern.PULL, ExerciseMode.REPS),
                    ExerciseOption("Renegade rows", ExercisePattern.PULL, ExerciseMode.REPS)
                ),
                ExercisePattern.LEGS to listOf(
                    ExerciseOption("Goblet squat", ExercisePattern.LEGS, ExerciseMode.REPS),
                    ExerciseOption("DB lunges", ExercisePattern.LEGS, ExerciseMode.REPS),
                    ExerciseOption("DB RDL", ExercisePattern.LEGS, ExerciseMode.REPS)
                ),
                ExercisePattern.CORE to listOf(
                    ExerciseOption("Russian twists", ExercisePattern.CORE, ExerciseMode.REPS),
                    ExerciseOption("Plank", ExercisePattern.CORE, ExerciseMode.TIME)
                ),
                ExercisePattern.CARDIO to listOf(
                    ExerciseOption("DB thrusters", ExercisePattern.CARDIO, ExerciseMode.REPS),
                    ExerciseOption("Jump rope", ExercisePattern.CARDIO, ExerciseMode.TIME)
                ),
                ExercisePattern.FULL_BODY to fullBody
            )
            EquipmentOption.FULL_GYM -> mapOf(
                ExercisePattern.PUSH to listOf(
                    ExerciseOption("Bench press", ExercisePattern.PUSH, ExerciseMode.REPS),
                    ExerciseOption("Incline bench press", ExercisePattern.PUSH, ExerciseMode.REPS),
                    ExerciseOption("Chest press machine", ExercisePattern.PUSH, ExerciseMode.REPS)
                ),
                ExercisePattern.PULL to listOf(
                    ExerciseOption("Lat pulldown", ExercisePattern.PULL, ExerciseMode.REPS),
                    ExerciseOption("Seated row", ExercisePattern.PULL, ExerciseMode.REPS),
                    ExerciseOption("Pull-ups", ExercisePattern.PULL, ExerciseMode.REPS)
                ),
                ExercisePattern.LEGS to listOf(
                    ExerciseOption("Back squat", ExercisePattern.LEGS, ExerciseMode.REPS),
                    ExerciseOption("Deadlift", ExercisePattern.LEGS, ExerciseMode.REPS),
                    ExerciseOption("Leg press", ExercisePattern.LEGS, ExerciseMode.REPS)
                ),
                ExercisePattern.CORE to listOf(
                    ExerciseOption("Cable crunch", ExercisePattern.CORE, ExerciseMode.REPS),
                    ExerciseOption("Hanging knee raises", ExercisePattern.CORE, ExerciseMode.REPS),
                    ExerciseOption("Plank", ExercisePattern.CORE, ExerciseMode.TIME)
                ),
                ExercisePattern.CARDIO to listOf(
                    ExerciseOption("Rower", ExercisePattern.CARDIO, ExerciseMode.TIME),
                    ExerciseOption("Incline treadmill", ExercisePattern.CARDIO, ExerciseMode.TIME),
                    ExerciseOption("Assault bike", ExercisePattern.CARDIO, ExerciseMode.TIME)
                ),
                ExercisePattern.FULL_BODY to fullBody + listOf(
                    ExerciseOption("Kettlebell swings", ExercisePattern.FULL_BODY, ExerciseMode.REPS)
                )
            )
        }
    }

    private fun pickExercise(
        options: List<ExerciseOption>,
        random: Random,
        avoidExerciseNames: Set<String>,
        used: MutableSet<String>
    ): ExerciseOption {
        val preferred = options.filter { it.name !in avoidExerciseNames && it.name !in used }
        val fallback = options.filter { it.name !in used }
        val pool = if (preferred.isNotEmpty()) preferred else fallback.ifEmpty { options }
        val choice = pool[random.nextInt(pool.size)]
        used.add(choice.name)
        return choice
    }

    private fun toGeneratedExercise(
        option: ExerciseOption,
        profile: GoalProfile,
        style: TrainingStyle?,
        random: Random
    ): GeneratedExercise {
        val rest = restSeconds(profile, style)
        return if (option.mode == ExerciseMode.TIME || profile.useTimedSets) {
            GeneratedExercise(
                name = option.name,
                seconds = profile.secondsRange.random(random),
                restSeconds = rest,
                notes = styleNote(style),
                intensityTag = profile.intensityTag,
                difficultyTag = profile.difficultyTag
            )
        } else {
            GeneratedExercise(
                name = option.name,
                sets = profile.sets,
                reps = profile.repsRange.random(random),
                restSeconds = rest,
                notes = styleNote(style),
                intensityTag = profile.intensityTag,
                difficultyTag = profile.difficultyTag
            )
        }
    }

    private fun restSeconds(profile: GoalProfile, style: TrainingStyle?): Int {
        val base = profile.restRange.random()
        return when (style) {
            TrainingStyle.CIRCUIT, TrainingStyle.AMRAP, TrainingStyle.EMOM -> (base - 15).coerceAtLeast(15)
            TrainingStyle.SUPERSETS -> (base - 10).coerceAtLeast(20)
            else -> base
        }
    }

    private fun styleNote(style: TrainingStyle?): String? {
        return when (style) {
            TrainingStyle.CIRCUIT -> "Circuit pace"
            TrainingStyle.SUPERSETS -> "Back-to-back sets"
            TrainingStyle.AMRAP -> "As many rounds as possible"
            TrainingStyle.EMOM -> "Start each minute"
            TrainingStyle.STRAIGHT_SETS -> "Controlled tempo"
            null -> null
        }
    }

    private fun patternSequence(focus: WorkoutFocus?): List<ExercisePattern> {
        return when (focus) {
            WorkoutFocus.UPPER -> listOf(
                ExercisePattern.PUSH,
                ExercisePattern.PULL,
                ExercisePattern.PUSH,
                ExercisePattern.PULL,
                ExercisePattern.CORE,
                ExercisePattern.CARDIO
            )
            WorkoutFocus.LOWER -> listOf(
                ExercisePattern.LEGS,
                ExercisePattern.LEGS,
                ExercisePattern.PUSH,
                ExercisePattern.CORE,
                ExercisePattern.CARDIO,
                ExercisePattern.PULL
            )
            WorkoutFocus.PULLUPS -> listOf(
                ExercisePattern.PULL,
                ExercisePattern.PUSH,
                ExercisePattern.PULL,
                ExercisePattern.LEGS,
                ExercisePattern.CORE,
                ExercisePattern.CARDIO
            )
            WorkoutFocus.CORE -> listOf(
                ExercisePattern.CORE,
                ExercisePattern.PUSH,
                ExercisePattern.CORE,
                ExercisePattern.LEGS,
                ExercisePattern.CARDIO,
                ExercisePattern.PULL
            )
            WorkoutFocus.FULL_BODY, null -> listOf(
                ExercisePattern.PUSH,
                ExercisePattern.PULL,
                ExercisePattern.LEGS,
                ExercisePattern.CORE,
                ExercisePattern.CARDIO,
                ExercisePattern.FULL_BODY
            )
        }
    }

    private fun patternForName(name: String): ExercisePattern {
        return when {
            name.contains("row", ignoreCase = true) -> ExercisePattern.PULL
            name.contains("pull", ignoreCase = true) -> ExercisePattern.PULL
            name.contains("bench", ignoreCase = true) -> ExercisePattern.PUSH
            name.contains("press", ignoreCase = true) -> ExercisePattern.PUSH
            name.contains("squat", ignoreCase = true) -> ExercisePattern.LEGS
            name.contains("deadlift", ignoreCase = true) -> ExercisePattern.LEGS
            name.contains("plank", ignoreCase = true) -> ExercisePattern.CORE
            name.contains("core", ignoreCase = true) -> ExercisePattern.CORE
            name.contains("treadmill", ignoreCase = true) -> ExercisePattern.CARDIO
            name.contains("rower", ignoreCase = true) -> ExercisePattern.CARDIO
            name.contains("bike", ignoreCase = true) -> ExercisePattern.CARDIO
            else -> ExercisePattern.FULL_BODY
        }
    }

    private data class DurationProfile(
        val warmupMinutes: Int,
        val mainMinutes: Int,
        val finisherMinutes: Int,
        val cooldownMinutes: Int
    )

    private data class GoalProfile(
        val repsRange: IntRange,
        val secondsRange: IntRange,
        val restRange: IntRange,
        val intensityTag: String,
        val useTimedSets: Boolean = false
    ) {
        val sets: Int = when {
            repsRange.first <= 6 -> 5
            repsRange.first <= 10 -> 4
            else -> 3
        }

        val difficultyTag: String = when {
            repsRange.first <= 6 -> "Hard"
            repsRange.first <= 12 -> "Moderate"
            else -> "Fast"
        }
    }

    private data class ExerciseOption(
        val name: String,
        val pattern: ExercisePattern,
        val mode: ExerciseMode
    )

    private enum class ExercisePattern {
        PUSH,
        PULL,
        LEGS,
        CORE,
        CARDIO,
        FULL_BODY
    }

    private enum class ExerciseMode {
        REPS,
        TIME
    }
}
