package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pushprime.model.NutritionEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.nutritionEntriesStore by preferencesDataStore(name = "nutrition_entries")

class NutritionRepository(
    private val context: Context
) {
    private val nutritionDao: NutritionDao? = runCatching {
        AppDatabase.getDatabase(context).nutritionDao()
    }.getOrNull()

    private object Keys {
        val EntriesJson = stringPreferencesKey("entries_json")
    }

    fun getEntriesForDate(date: String): Flow<List<NutritionEntry>> {
        return nutritionDao?.getEntriesForDate(date) ?: context.nutritionEntriesStore.data.map { prefs ->
            parseEntries(prefs[Keys.EntriesJson]).filter { it.date == date }
        }
    }

    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<NutritionEntry>> {
        return nutritionDao?.getEntriesForDateRange(startDate, endDate)
            ?: context.nutritionEntriesStore.data.map { prefs ->
                parseEntries(prefs[Keys.EntriesJson]).filter { it.date in startDate..endDate }
            }
    }

    fun getAllEntries(): Flow<List<NutritionEntry>> {
        return nutritionDao?.getAllEntries() ?: context.nutritionEntriesStore.data.map { prefs ->
            parseEntries(prefs[Keys.EntriesJson])
        }
    }

    suspend fun getAllEntriesOnce(): List<NutritionEntry> {
        return nutritionDao?.getAllEntriesOnce()
            ?: run {
                val prefs = context.nutritionEntriesStore.data.first()
                parseEntries(prefs[Keys.EntriesJson])
            }
    }

    suspend fun getEntriesForDateOnce(date: String): List<NutritionEntry> {
        return nutritionDao?.getEntriesForDateOnce(date)
            ?: run {
                val prefs = context.nutritionEntriesStore.data.first()
                parseEntries(prefs[Keys.EntriesJson]).filter { it.date == date }
            }
    }

    suspend fun getEntriesForDateRangeOnce(startDate: String, endDate: String): List<NutritionEntry> {
        return nutritionDao?.getEntriesForDateRangeOnce(startDate, endDate)
            ?: run {
                val prefs = context.nutritionEntriesStore.data.first()
                parseEntries(prefs[Keys.EntriesJson]).filter { it.date in startDate..endDate }
            }
    }

    suspend fun insert(entry: NutritionEntry) {
        if (nutritionDao != null) {
            nutritionDao.insert(entry)
            return
        }
        context.nutritionEntriesStore.edit { prefs ->
            val existing = parseEntries(prefs[Keys.EntriesJson]).toMutableList()
            existing.add(0, entry)
            prefs[Keys.EntriesJson] = serializeEntries(existing)
        }
    }

    private fun parseEntries(raw: String?): List<NutritionEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        NutritionEntry(
                            id = obj.getString("id"),
                            date = obj.getString("date"),
                            mealType = obj.optString("mealType", "SNACK"),
                            name = obj.optString("name", ""),
                            calories = obj.optInt("calories").takeIf { obj.has("calories") },
                            proteinGrams = obj.optInt("proteinGrams").takeIf { obj.has("proteinGrams") },
                            notes = obj.optString("notes", "").takeIf { it.isNotBlank() },
                            createdAt = obj.optLong("createdAt")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeEntries(entries: List<NutritionEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date)
                put("mealType", entry.mealType)
                put("name", entry.name)
                entry.calories?.let { put("calories", it) }
                entry.proteinGrams?.let { put("proteinGrams", it) }
                entry.notes?.let { put("notes", it) }
                put("createdAt", entry.createdAt)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
