package com.pushprime.data

import com.pushprime.model.GeneratedWorkoutPlan
import com.pushprime.model.GeneratedWorkoutPlanEntity
import com.pushprime.model.GeneratedWorkoutPlanSummary
import com.pushprime.model.EquipmentOption
import com.pushprime.model.WorkoutFocus
import com.pushprime.model.WorkoutGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class WorkoutPlanRecord(
    val plan: GeneratedWorkoutPlan,
    val isSaved: Boolean
)

class WorkoutPlanRepository(
    private val workoutPlanDao: WorkoutPlanDao
) {
    suspend fun createDraft(plan: GeneratedWorkoutPlan): Long {
        val entity = toEntity(plan, isSaved = false)
        return workoutPlanDao.insert(entity)
    }

    suspend fun updateDraft(planId: Long, plan: GeneratedWorkoutPlan, isSaved: Boolean): GeneratedWorkoutPlan {
        val existing = workoutPlanDao.getById(planId)
        val entity = toEntity(
            plan.copy(id = planId),
            isSaved = isSaved,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        workoutPlanDao.update(entity)
        return plan.copy(id = planId)
    }

    suspend fun markSaved(planId: Long) {
        val existing = workoutPlanDao.getById(planId) ?: return
        workoutPlanDao.update(existing.copy(isSaved = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun getPlan(planId: Long): GeneratedWorkoutPlan? {
        return workoutPlanDao.getById(planId)?.let { entity ->
            WorkoutPlanJson.decode(entity.planJson).copy(id = entity.id)
        }
    }

    suspend fun getPlanRecord(planId: Long): WorkoutPlanRecord? {
        return workoutPlanDao.getById(planId)?.let { entity ->
            WorkoutPlanRecord(
                plan = WorkoutPlanJson.decode(entity.planJson).copy(id = entity.id),
                isSaved = entity.isSaved
            )
        }
    }

    fun getSavedPlans(): Flow<List<GeneratedWorkoutPlanSummary>> {
        return workoutPlanDao.getSavedPlans().map { plans ->
            plans.map { entity ->
                GeneratedWorkoutPlanSummary(
                    id = entity.id,
                    title = entity.title,
                    totalDurationMinutes = entity.totalDurationMinutes,
                    goal = WorkoutGoal.valueOf(entity.goal),
                    equipment = EquipmentOption.valueOf(entity.equipment),
                    focus = entity.focus?.let { WorkoutFocus.valueOf(it) }
                )
            }
        }
    }

    suspend fun deleteDrafts() {
        workoutPlanDao.deleteDrafts()
    }

    private fun toEntity(plan: GeneratedWorkoutPlan, isSaved: Boolean, createdAt: Long = System.currentTimeMillis()): GeneratedWorkoutPlanEntity {
        val signature = plan.blocks.flatMap { block -> block.exercises.map { it.name } }.joinToString("|")
        return GeneratedWorkoutPlanEntity(
            id = plan.id,
            title = plan.title,
            totalDurationMinutes = plan.totalDurationMinutes,
            goal = plan.goal.name,
            timeMinutes = plan.timeMinutes,
            equipment = plan.equipment.name,
            focus = plan.focus?.name,
            style = plan.style?.name,
            planJson = WorkoutPlanJson.encode(plan),
            exerciseSignature = signature,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis(),
            isSaved = isSaved
        )
    }
}
