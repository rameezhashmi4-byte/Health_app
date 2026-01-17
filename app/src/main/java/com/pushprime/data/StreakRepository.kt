package com.pushprime.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pushprime.model.DailyStatusEntity
import com.pushprime.model.DailyStatusType
import com.pushprime.model.StreakState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val Context.streakDataStore by preferencesDataStore(name = "streak_state")

class StreakRepository(
    private val context: Context,
    private val sessionDao: SessionDao,
    private val dailyStatusDao: DailyStatusDao,
    private val nutritionRepository: NutritionRepository,
    private val pullupSessionDao: PullupSessionDao,
    private val pullupMaxTestDao: PullupMaxTestDao
) {
    companion object {
        const val DEFAULT_FREEZE_TOKENS = 2
        const val MAX_REST_DAYS_PER_WEEK = 2
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    private object Keys {
        val CurrentStreakDays = intPreferencesKey("current_streak_days")
        val LongestStreakDays = intPreferencesKey("longest_streak_days")
        val LastWorkoutDate = stringPreferencesKey("last_workout_date")
        val LastStreakProtectedDate = stringPreferencesKey("last_streak_protected_date")
        val FreezeTokensRemaining = intPreferencesKey("freeze_tokens_remaining")
        val RestDaysUsedThisWeek = intPreferencesKey("rest_days_used_this_week")
        val LastTokenResetMonth = stringPreferencesKey("last_token_reset_month")
        val LastEvaluatedDate = stringPreferencesKey("last_evaluated_date")
    }

    val streakState: Flow<StreakState> = context.streakDataStore.data.map { prefs ->
        prefs.toStreakState()
    }

    fun observeStatusForDate(date: LocalDate): Flow<DailyStatusEntity?> {
        return dailyStatusDao.observeStatusForDate(date.format(dateFormatter))
    }

    fun observeStatusesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyStatusEntity>> {
        return dailyStatusDao.observeStatusesBetween(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )
    }

    suspend fun evaluateStreak(today: LocalDate = LocalDate.now(), force: Boolean = false): StreakState {
        val prefs = context.streakDataStore.data.first()
        val currentMonth = YearMonth.from(today).format(monthFormatter)

        var currentStreak = prefs[Keys.CurrentStreakDays] ?: 0
        var longestStreak = prefs[Keys.LongestStreakDays] ?: 0
        var lastWorkoutDate = prefs[Keys.LastWorkoutDate] ?: ""
        var lastStreakProtectedDate = prefs[Keys.LastStreakProtectedDate]
        var freezeTokensRemaining = prefs[Keys.FreezeTokensRemaining] ?: DEFAULT_FREEZE_TOKENS
        var lastTokenResetMonth = prefs[Keys.LastTokenResetMonth] ?: ""

        if (lastWorkoutDate.isBlank()) {
            val sessions = sessionDao.getAllSessionsOnce()
            val nutritionDates = nutritionRepository.getAllEntriesOnce()
                .filter { (it.calories ?: 0) > 0 || (it.proteinGrams ?: 0) > 0 }
                .map { it.date }
            val pullupDates = pullupSessionDao.getAllSessionsOnce()
                .map { dateFromTimestamp(it.dateTime) }
            val pullupTestDates = pullupMaxTestDao.getAllTestsOnce()
                .map { dateFromTimestamp(it.dateTime) }
            val allDates = (sessions.map { it.date } + nutritionDates + pullupDates + pullupTestDates)
                .distinct()
                .sortedDescending()

            if (allDates.isNotEmpty()) {
                val todayStr = today.format(dateFormatter)
                val yesterdayStr = today.minusDays(1).format(dateFormatter)
                if (allDates.first() == todayStr || allDates.first() == yesterdayStr) {
                    var streakSeed = 0
                    var cursor = today
                    while (allDates.contains(cursor.format(dateFormatter))) {
                        streakSeed += 1
                        cursor = cursor.minusDays(1)
                    }
                    currentStreak = streakSeed
                }
                lastWorkoutDate = allDates.first()
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
            }
        }

        if (lastTokenResetMonth.isBlank() || lastTokenResetMonth != currentMonth) {
            freezeTokensRemaining = DEFAULT_FREEZE_TOKENS
            lastTokenResetMonth = currentMonth
        }

        val lastEvaluatedStr = prefs[Keys.LastEvaluatedDate].orEmpty()
        val lastEvaluated = lastEvaluatedStr.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull()
        }

        val startDate = when {
            force -> today
            lastEvaluated == null -> today
            lastEvaluated.isAfter(today) -> today
            lastEvaluated.isEqual(today) -> null
            else -> lastEvaluated.plusDays(1)
        }

        if (startDate != null && !startDate.isAfter(today)) {
            var date = startDate
            while (!date.isAfter(today)) {
                val dateStr = date.format(dateFormatter)
                val activityCount = getActivityCountForDate(date)
                val existingStatus = dailyStatusDao.getStatusForDate(dateStr)
                val existingType = existingStatus?.status?.let { status ->
                    runCatching { DailyStatusType.valueOf(status) }.getOrNull()
                }

                val derivedStatus = when {
                    activityCount > 0 || existingType == DailyStatusType.WORKOUT -> DailyStatusType.WORKOUT
                    existingType == DailyStatusType.REST -> DailyStatusType.REST
                    existingType == DailyStatusType.FROZEN -> DailyStatusType.FROZEN
                    else -> DailyStatusType.MISSED
                }

                var finalStatus = derivedStatus

                when (derivedStatus) {
                    DailyStatusType.WORKOUT -> {
                        val lastWorkout = lastWorkoutDate.takeIf { it.isNotBlank() }?.let {
                            runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull()
                        }
                        currentStreak = when {
                            lastWorkout == null -> 1
                            lastWorkout.isEqual(date) -> currentStreak
                            lastWorkout.isEqual(date.minusDays(1)) -> currentStreak + 1
                            else -> 1
                        }
                        lastWorkoutDate = dateStr
                        if (currentStreak > longestStreak) {
                            longestStreak = currentStreak
                        }
                    }
                    DailyStatusType.REST -> {
                        // Rest day keeps streak unchanged.
                    }
                    DailyStatusType.FROZEN -> {
                        if (lastStreakProtectedDate != dateStr) {
                            lastStreakProtectedDate = dateStr
                        }
                    }
                    DailyStatusType.MISSED -> {
                        if (currentStreak == 0) {
                            // No active streak to protect.
                        } else if (freezeTokensRemaining > 0 && lastStreakProtectedDate != dateStr) {
                            freezeTokensRemaining -= 1
                            lastStreakProtectedDate = dateStr
                            finalStatus = DailyStatusType.FROZEN
                        } else {
                            currentStreak = 0
                        }
                    }
                }

                dailyStatusDao.upsert(
                    DailyStatusEntity(
                        date = dateStr,
                        status = finalStatus.name,
                        sessionCount = activityCount
                    )
                )
                date = date.plusDays(1)
            }
        }

        val weekStart = today.with(DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(6)
        val restDaysUsedThisWeek = dailyStatusDao.countStatusBetween(
            DailyStatusType.REST.name,
            weekStart.format(dateFormatter),
            weekEnd.format(dateFormatter)
        )

        val updatedState = StreakState(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            lastWorkoutDate = lastWorkoutDate,
            lastStreakProtectedDate = lastStreakProtectedDate,
            freezeTokensRemaining = freezeTokensRemaining,
            restDaysUsedThisWeek = restDaysUsedThisWeek,
            lastTokenResetMonth = lastTokenResetMonth
        )

        context.streakDataStore.edit { store ->
            store[Keys.CurrentStreakDays] = updatedState.currentStreakDays
            store[Keys.LongestStreakDays] = updatedState.longestStreakDays
            store[Keys.LastWorkoutDate] = updatedState.lastWorkoutDate
            updatedState.lastStreakProtectedDate?.let { store[Keys.LastStreakProtectedDate] = it }
                ?: store.remove(Keys.LastStreakProtectedDate)
            store[Keys.FreezeTokensRemaining] = updatedState.freezeTokensRemaining
            store[Keys.RestDaysUsedThisWeek] = updatedState.restDaysUsedThisWeek
            store[Keys.LastTokenResetMonth] = updatedState.lastTokenResetMonth
            store[Keys.LastEvaluatedDate] = today.format(dateFormatter)
        }

        return updatedState
    }

    suspend fun markRestDay(today: LocalDate = LocalDate.now()): Boolean {
        val dateStr = today.format(dateFormatter)
        if (hasWorkoutActivityForDate(today)) {
            return false
        }

        val existing = dailyStatusDao.getStatusForDate(dateStr)
        if (existing?.status == DailyStatusType.REST.name) {
            evaluateStreak(today, force = true)
            return true
        }

        val weekStart = today.with(DayOfWeek.MONDAY)
        val weekEnd = weekStart.plusDays(6)
        val restDaysUsed = dailyStatusDao.countStatusBetween(
            DailyStatusType.REST.name,
            weekStart.format(dateFormatter),
            weekEnd.format(dateFormatter)
        )
        if (restDaysUsed >= MAX_REST_DAYS_PER_WEEK) {
            return false
        }

        dailyStatusDao.upsert(
            DailyStatusEntity(
                date = dateStr,
                status = DailyStatusType.REST.name,
                sessionCount = 0
            )
        )
        evaluateStreak(today, force = true)
        return true
    }

    private suspend fun getActivityCountForDate(date: LocalDate): Int {
        val dateStr = date.format(dateFormatter)
        val workoutCount = sessionDao.getSessionCountForDate(dateStr)
        val nutritionCount = nutritionRepository.getEntriesForDateOnce(dateStr)
            .count { (it.calories ?: 0) > 0 || (it.proteinGrams ?: 0) > 0 }
        val dayRange = getDayRangeMillis(date)
        val pullupCount = pullupSessionDao.getSessionsForRangeOnce(dayRange.first, dayRange.second).size
        val pullupTestCount = pullupMaxTestDao.getTestsForRangeOnce(dayRange.first, dayRange.second).size
        return workoutCount + nutritionCount + pullupCount + pullupTestCount
    }

    private suspend fun hasWorkoutActivityForDate(date: LocalDate): Boolean {
        val dateStr = date.format(dateFormatter)
        if (sessionDao.getSessionCountForDate(dateStr) > 0) return true
        val dayRange = getDayRangeMillis(date)
        if (pullupSessionDao.getSessionsForRangeOnce(dayRange.first, dayRange.second).isNotEmpty()) return true
        if (pullupMaxTestDao.getTestsForRangeOnce(dayRange.first, dayRange.second).isNotEmpty()) return true
        return false
    }

    private fun getDayRangeMillis(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start to end
    }

    private fun Preferences.toStreakState(): StreakState {
        return StreakState(
            currentStreakDays = this[Keys.CurrentStreakDays] ?: 0,
            longestStreakDays = this[Keys.LongestStreakDays] ?: 0,
            lastWorkoutDate = this[Keys.LastWorkoutDate] ?: "",
            lastStreakProtectedDate = this[Keys.LastStreakProtectedDate],
            freezeTokensRemaining = this[Keys.FreezeTokensRemaining] ?: DEFAULT_FREEZE_TOKENS,
            restDaysUsedThisWeek = this[Keys.RestDaysUsedThisWeek] ?: 0,
            lastTokenResetMonth = this[Keys.LastTokenResetMonth] ?: ""
        )
    }
}
