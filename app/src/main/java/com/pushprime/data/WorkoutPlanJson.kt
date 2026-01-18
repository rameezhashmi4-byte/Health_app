package com.pushprime.data

import com.pushprime.model.GeneratedExercise
import com.pushprime.model.GeneratedWorkoutPlan
import com.pushprime.model.TrainingStyle
import com.pushprime.model.WorkoutBlock
import com.pushprime.model.WorkoutBlockType
import com.pushprime.model.WorkoutFocus
import com.pushprime.model.WorkoutGoal
import com.pushprime.model.EquipmentOption
import org.json.JSONArray
import org.json.JSONObject

object WorkoutPlanJson {
    fun encode(plan: GeneratedWorkoutPlan): String {
        val root = JSONObject()
        root.put("id", plan.id)
        root.put("title", plan.title)
        root.put("totalDurationMinutes", plan.totalDurationMinutes)
        root.put("goal", plan.goal.name)
        root.put("timeMinutes", plan.timeMinutes)
        root.put("equipment", plan.equipment.name)
        root.put("focus", plan.focus?.name)
        root.put("style", plan.style?.name)

        val blocksJson = JSONArray()
        plan.blocks.forEach { block ->
            val blockJson = JSONObject()
            blockJson.put("type", block.type.name)
            blockJson.put("title", block.title)
            blockJson.put("durationMinutes", block.durationMinutes)
            val exercisesJson = JSONArray()
            block.exercises.forEach { exercise ->
                exercisesJson.put(encodeExercise(exercise))
            }
            blockJson.put("exercises", exercisesJson)
            blocksJson.put(blockJson)
        }
        root.put("blocks", blocksJson)
        return root.toString()
    }

    fun decode(json: String): GeneratedWorkoutPlan {
        val root = JSONObject(json)
        val blocksJson = root.getJSONArray("blocks")
        val blocks = mutableListOf<WorkoutBlock>()
        for (i in 0 until blocksJson.length()) {
            val blockJson = blocksJson.getJSONObject(i)
            val exercisesJson = blockJson.getJSONArray("exercises")
            val exercises = mutableListOf<GeneratedExercise>()
            for (j in 0 until exercisesJson.length()) {
                exercises.add(decodeExercise(exercisesJson.getJSONObject(j)))
            }
            blocks.add(
                WorkoutBlock(
                    type = WorkoutBlockType.valueOf(blockJson.getString("type")),
                    title = blockJson.getString("title"),
                    durationMinutes = blockJson.optInt("durationMinutes").takeIf { it > 0 },
                    exercises = exercises
                )
            )
        }

        return GeneratedWorkoutPlan(
            id = root.optLong("id", 0L),
            title = root.getString("title"),
            totalDurationMinutes = root.getInt("totalDurationMinutes"),
            goal = WorkoutGoal.valueOf(root.getString("goal")),
            timeMinutes = root.getInt("timeMinutes"),
            equipment = EquipmentOption.valueOf(root.getString("equipment")),
            focus = root.optString("focus").takeIf { it.isNotBlank() }?.let { WorkoutFocus.valueOf(it) },
            style = root.optString("style").takeIf { it.isNotBlank() }?.let { TrainingStyle.valueOf(it) },
            blocks = blocks
        )
    }

    private fun encodeExercise(exercise: GeneratedExercise): JSONObject {
        val json = JSONObject()
        json.put("name", exercise.name)
        json.put("sets", exercise.sets)
        json.put("reps", exercise.reps)
        json.put("seconds", exercise.seconds)
        json.put("restSeconds", exercise.restSeconds)
        json.put("notes", exercise.notes)
        json.put("intensityTag", exercise.intensityTag)
        json.put("difficultyTag", exercise.difficultyTag)
        exercise.caloriesEstimate?.let { json.put("caloriesEstimate", it) }
        return json
    }

    private fun decodeExercise(json: JSONObject): GeneratedExercise {
        return GeneratedExercise(
            name = json.getString("name"),
            sets = json.optInt("sets").takeIf { it > 0 },
            reps = json.optInt("reps").takeIf { it > 0 },
            seconds = json.optInt("seconds").takeIf { it > 0 },
            restSeconds = json.optInt("restSeconds", 0),
            notes = json.optString("notes").takeIf { it.isNotBlank() },
            intensityTag = json.optString("intensityTag").takeIf { it.isNotBlank() },
            difficultyTag = json.optString("difficultyTag").takeIf { it.isNotBlank() },
            caloriesEstimate = json.optInt("caloriesEstimate").takeIf { json.has("caloriesEstimate") }
        )
    }
}
