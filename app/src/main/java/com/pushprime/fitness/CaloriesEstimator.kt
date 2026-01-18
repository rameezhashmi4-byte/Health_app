package com.pushprime.fitness

import com.pushprime.model.WorkoutBlockType
import kotlin.math.roundToInt

/**
 * Lightweight calorie estimation using METs:
 * kcal = MET * weightKg * durationHours
 *
 * Notes:
 * - This is an estimate (not heart-rate/device measured).
 * - We keep it simple and deterministic for UI/analytics.
 */
object CaloriesEstimator {
    private const val DefaultWeightKg = 70.0
    private const val RestMet = 1.3

    fun resolveWeightKg(weightKg: Double?): Double {
        return (weightKg ?: 0.0).takeIf { it > 0.0 } ?: DefaultWeightKg
    }

    fun kcalForSeconds(met: Double, weightKg: Double, durationSeconds: Int): Int {
        val hours = (durationSeconds.coerceAtLeast(0) / 3600.0)
        val kcal = met.coerceIn(0.0, 30.0) * weightKg.coerceAtLeast(0.0) * hours
        return kcal.roundToInt().coerceAtLeast(0)
    }

    fun kcalForActiveAndRestSeconds(
        exerciseMet: Double,
        weightKg: Double,
        activeSeconds: Int,
        restSeconds: Int
    ): Int {
        val active = kcalForSeconds(exerciseMet, weightKg, activeSeconds)
        val rest = kcalForSeconds(RestMet, weightKg, restSeconds)
        return active + rest
    }

    /**
     * Estimate MET based on intensity tags + exercise name keywords + block type hints.
     */
    fun estimateMet(
        exerciseName: String,
        intensityTag: String? = null,
        blockType: WorkoutBlockType? = null
    ): Double {
        val name = exerciseName.lowercase()
        val tag = intensityTag?.trim()?.lowercase()

        // Base MET from tag (common generator tags: Light, Moderate, High, Max, Heavy, Strong, Intervals)
        var met = when {
            tag == null -> 4.5
            tag.contains("light") -> 3.0
            tag.contains("easy") -> 3.0
            tag.contains("moderate") -> 5.0
            tag.contains("heavy") -> 6.0
            tag.contains("strong") -> 6.0
            tag.contains("high") -> 8.0
            tag.contains("max") -> 10.0
            tag.contains("interval") -> 9.5
            else -> 5.0
        }

        // Mobility / stretching is low MET regardless of tag.
        if (name.contains("stretch") || name.contains("mobility") || name.contains("dynamic")) {
            met = minOf(met, 3.0)
        }

        // Cardio keywords
        val cardio = name.contains("rower") ||
            name.contains("treadmill") ||
            name.contains("assault bike") ||
            name.contains("bike") ||
            name.contains("jump rope") ||
            name.contains("high knees") ||
            name.contains("jumping jacks") ||
            name.contains("sprint") ||
            name.contains("run")
        if (cardio) {
            met = maxOf(met, 7.0)
        }

        // HIIT / full-body explosive
        val hiit = name.contains("burpee") ||
            name.contains("mountain climber") ||
            name.contains("hiit") ||
            name.contains("thruster") ||
            name.contains("kettlebell swing") ||
            name.contains("swings") ||
            name.contains("interval")
        if (hiit) {
            met = maxOf(met, 8.5)
        }

        // Strength / resistance movements often land mid-range unless tagged high/max.
        val strength = name.contains("bench") ||
            name.contains("press") ||
            name.contains("deadlift") ||
            name.contains("squat") ||
            name.contains("row") ||
            name.contains("pulldown") ||
            name.contains("pull-up") ||
            name.contains("pullups") ||
            name.contains("lunge") ||
            name.contains("curl") ||
            name.contains("triceps")
        if (strength && !cardio && !hiit) {
            met = met.coerceIn(3.5, 8.0)
        }

        // Planks are moderate.
        if (name.contains("plank")) {
            met = met.coerceIn(3.0, 5.0)
        }

        // Block hints: warmups/cooldowns usually aren't vigorous.
        if (blockType == WorkoutBlockType.WARMUP || blockType == WorkoutBlockType.COOLDOWN) {
            met = minOf(met, 5.5)
        }

        return met.coerceIn(2.0, 12.0)
    }

    /**
     * If a plan stored `caloriesEstimate` assuming 70kg, scale it for the user's weight.
     */
    fun scaleCaloriesFrom70Kg(baselineFor70Kg: Int?, weightKg: Double): Int? {
        val base = baselineFor70Kg ?: return null
        if (base <= 0) return 0
        return ((base.toDouble() * (weightKg / DefaultWeightKg))).roundToInt().coerceAtLeast(0)
    }
}

