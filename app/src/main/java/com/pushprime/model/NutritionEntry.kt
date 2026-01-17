package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(tableName = "nutrition_entries")
data class NutritionEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val mealType: String = MealType.SNACK.name,
    val name: String = "",
    val calories: Int? = null,
    val proteinGrams: Int? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}
