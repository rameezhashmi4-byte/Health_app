package com.pushprime.data

import android.content.Context

class StepsRepository(
    context: Context
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
}
