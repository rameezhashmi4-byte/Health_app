package com.pushprime.data.meal

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

class RegionTemplateLoader(
    private val gson: Gson = Gson()
) {
    fun load(context: Context, region: Region): RegionTemplate {
        val assetPath = "meal_templates/${region.assetFileName}"
        context.assets.open(assetPath).use { stream ->
            InputStreamReader(stream).use { reader ->
                return gson.fromJson(reader, RegionTemplate::class.java)
            }
        }
    }

    fun loadAll(context: Context): List<RegionTemplate> {
        return Region.values().map { region ->
            load(context, region)
        }
    }
}
