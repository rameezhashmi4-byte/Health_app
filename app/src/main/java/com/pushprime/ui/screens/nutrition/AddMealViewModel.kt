package com.pushprime.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.NutritionRepository
import com.pushprime.model.MealType
import com.pushprime.model.NutritionEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMealViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    fun saveMeal(
        mealType: MealType,
        name: String,
        calories: Int?,
        protein: Int?,
        notes: String?
    ) {
        viewModelScope.launch {
            nutritionRepository.insert(
                NutritionEntry(
                    mealType = mealType.name,
                    name = name.trim(),
                    calories = calories,
                    proteinGrams = protein,
                    notes = notes?.takeIf { it.isNotBlank() }
                )
            )
        }
    }
}
