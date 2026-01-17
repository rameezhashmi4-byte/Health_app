package com.pushprime.data

import android.content.Context
import java.time.LocalDate
import java.time.ZoneId
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val healthConnectHelper = HealthConnectHelper(context)

    suspend fun getTodaySteps(): Long {
        return if (healthConnectHelper.isAvailable) {
            healthConnectHelper.getTodaySteps()
        } else {
            0L
        }
    }

    suspend fun getWeekSteps(): Long {
        return if (healthConnectHelper.isAvailable) {
            healthConnectHelper.getWeekSteps()
        } else {
            0L
        }
    }

    suspend fun getLast7DaysSteps(): List<StepsDay> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val days = (6 downTo 0).map { offset ->
            today.minusDays(offset.toLong())
        }
        return days.map { date ->
            val start = date.atStartOfDay(zone).toInstant()
            val end = date.plusDays(1).atStartOfDay(zone).toInstant()
            val steps = if (healthConnectHelper.isAvailable) {
                healthConnectHelper.getSteps(start, end)
            } else {
                0L
            }
            StepsDay(date, steps)
        }
    }
}

data class StepsDay(
    val date: LocalDate,
    val steps: Long
)
