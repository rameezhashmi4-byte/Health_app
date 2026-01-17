package com.pushprime.data

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromIntList(values: List<Int>?): String {
        if (values.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        values.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toIntList(raw: String?): List<Int> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    add(array.getInt(i))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
