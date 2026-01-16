package com.pushprime.data.meal

import android.content.Context
import com.pushprime.model.CaloriesResult
import kotlin.math.roundToInt

class MealPlanGenerator(
    private val templateLoader: RegionTemplateLoader = RegionTemplateLoader()
) {
    fun generate(
        context: Context,
        caloriesResult: CaloriesResult,
        region: Region,
        preferences: MealPreferences = MealPreferences()
    ): MealPlan {
        val template = templateLoader.load(context, region)
        val targets = calorieTargets(caloriesResult.dailyCalories)
        val meals = listOf(
            planMeal(MealType.BREAKFAST, targets.breakfast, template, preferences),
            planMeal(MealType.LUNCH, targets.lunch, template, preferences),
            planMeal(MealType.DINNER, targets.dinner, template, preferences),
            planMeal(MealType.SNACK, targets.snack, template, preferences)
        )

        val swapSuggestions = template.swapRules
            .filter { isAllowed(it.dish, preferences) && isAllowed(it.swapWith, preferences) }
            .map { SwapSuggestion(it.dish, it.swapWith, it.reason) }

        val restaurantSuggestions = template.restaurantMode
            .filter { isAllowed(it, preferences) }

        return MealPlan(
            region = region,
            targetCalories = caloriesResult.dailyCalories,
            meals = meals,
            swapSuggestions = swapSuggestions,
            restaurantModeSuggestions = restaurantSuggestions
        )
    }

    private fun planMeal(
        type: MealType,
        targetCalories: Int,
        template: RegionTemplate,
        preferences: MealPreferences
    ): PlannedMeal {
        val candidateMeals = template.commonMeals
            .filter { it.mealType == type }
            .filter { isAllowed(it.name, preferences) }
            .filter { isAllowed(it.portionGuidance, preferences) }

        val selected = candidateMeals.firstOrNull()
        if (selected != null) {
            return PlannedMeal(
                mealType = type,
                title = selected.name,
                portionGuidance = selected.portionGuidance,
                targetCalories = targetCalories,
                components = listOfNotNull(selected.description)
            )
        }

        val protein = pickOption(
            options = template.proteinOptions,
            preferred = preferences.preferredProteins,
            preferences = preferences
        )
        val carb = pickOption(
            options = template.carbOptions,
            preferred = preferences.preferredCarbs,
            preferences = preferences
        )
        val fat = pickOption(
            options = template.fatOptions,
            preferred = preferences.preferredFats,
            preferences = preferences
        )

        val components = listOfNotNull(
            protein?.let { "Protein: $it" },
            carb?.let { "Carb: $it" },
            fat?.let { "Fat: $it" }
        )

        return PlannedMeal(
            mealType = type,
            title = fallbackTitle(type, components),
            portionGuidance = fallbackPortions(type),
            targetCalories = targetCalories,
            components = components
        )
    }

    private fun pickOption(
        options: List<String>,
        preferred: Set<String>,
        preferences: MealPreferences
    ): String? {
        val filtered = options
            .filter { isAllowed(it, preferences) }
            .filterNot { preferences.vegetarian && containsMeat(it) }

        if (filtered.isEmpty()) return null
        if (preferred.isEmpty()) return filtered.first()

        val match = filtered.firstOrNull { option ->
            preferred.any { preferredItem ->
                option.contains(preferredItem, ignoreCase = true)
            }
        }
        return match ?: filtered.first()
    }

    private fun isAllowed(text: String, preferences: MealPreferences): Boolean {
        if (preferences.excludedIngredients.isEmpty()) return true
        return preferences.excludedIngredients.none { excluded ->
            text.contains(excluded, ignoreCase = true)
        }
    }

    private fun containsMeat(text: String): Boolean {
        val meatKeywords = listOf(
            "chicken", "beef", "lamb", "pork", "turkey", "duck",
            "salmon", "tuna", "fish", "shrimp", "prawn", "mutton", "goat"
        )
        return meatKeywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun fallbackTitle(type: MealType, components: List<String>): String {
        if (components.isEmpty()) {
            return when (type) {
                MealType.SNACK -> "Simple snack"
                else -> "Balanced plate"
            }
        }
        return when (type) {
            MealType.SNACK -> "Snack plate"
            else -> "Balanced plate"
        }
    }

    private fun fallbackPortions(type: MealType): String {
        return when (type) {
            MealType.SNACK -> "1 small portion from each component."
            else -> "1 palm protein, 1 fist carbs, 1 thumb fat."
        }
    }

    private fun calorieTargets(total: Int): CalorieTargets {
        val breakfast = (total * 0.25f).roundToInt()
        val lunch = (total * 0.3f).roundToInt()
        val dinner = (total * 0.3f).roundToInt()
        val snack = total - breakfast - lunch - dinner
        return CalorieTargets(breakfast, lunch, dinner, snack)
    }

    private data class CalorieTargets(
        val breakfast: Int,
        val lunch: Int,
        val dinner: Int,
        val snack: Int
    )
}
