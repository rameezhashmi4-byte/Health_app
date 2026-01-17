package com.pushprime.data

import com.pushprime.model.Achievement
import com.pushprime.model.AchievementEntity
import com.pushprime.model.AchievementType
import com.pushprime.model.ActivityType
import com.pushprime.model.Intensity
import com.pushprime.model.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class AchievementStats(
    val streakDays: Int,
    val totalSessions: Int,
    val sportsSessions: Int,
    val highEffortSports: Int,
    val todaySteps: Int,
    val stepsStreakDays: Int
)

data class AchievementSummary(
    val totalUnlocked: Int,
    val totalBadges: Int,
    val currentStreak: Int,
    val sessionsCompleted: Int
)

private data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val threshold: Int,
    val icon: String,
    val progressSelector: (AchievementStats) -> Int
)

@Singleton
class AchievementsRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val sessionDao: SessionDao,
    private val stepsRepository: StepsRepository
) {
    private val popupEmitter = MutableSharedFlow<Achievement>(extraBufferCapacity = 1)
    val popupEvents = popupEmitter.asSharedFlow()

    fun observeAchievements(): Flow<List<Achievement>> {
        return achievementDao.observeAll().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun recalcAchievements(
        todayStepsOverride: Int? = null,
        stepsStreakOverride: Int? = null,
        emitPopup: Boolean = false
    ): List<Achievement> {
        val sessions = sessionDao.getAllSessionsOnce()
        val stats = buildStats(
            sessions = sessions,
            todayStepsOverride = todayStepsOverride,
            stepsStreakOverride = stepsStreakOverride
        )

        val existing = achievementDao.getAll().associateBy { it.id }
        val now = System.currentTimeMillis()
        val updates = definitions.map { definition ->
            val rawProgress = definition.progressSelector(stats)
            val cappedProgress = rawProgress.coerceAtMost(definition.threshold)
            val previous = existing[definition.id]
            val wasUnlocked = previous?.unlocked == true
            val unlockedNow = wasUnlocked || rawProgress >= definition.threshold
            val unlockedAt = when {
                wasUnlocked -> previous?.unlockedAt
                unlockedNow -> now
                else -> null
            }
            AchievementEntity(
                id = definition.id,
                title = definition.title,
                description = definition.description,
                type = definition.type.name,
                threshold = definition.threshold,
                progress = cappedProgress,
                unlocked = unlockedNow,
                unlockedAt = unlockedAt,
                icon = definition.icon
            )
        }

        achievementDao.upsertAll(updates)

        val newlyUnlocked = updates.filter { update ->
            update.unlocked && (existing[update.id]?.unlocked != true)
        }

        if (emitPopup && newlyUnlocked.isNotEmpty()) {
            popupEmitter.tryEmit(newlyUnlocked.first().toModel())
        }

        return newlyUnlocked.map { it.toModel() }
    }

    suspend fun getSummary(): AchievementSummary {
        val sessions = sessionDao.getAllSessionsOnce()
        val stats = buildStats(sessions, todayStepsOverride = null, stepsStreakOverride = null)
        val achievements = achievementDao.getAll()
        val unlocked = achievements.count { it.unlocked }
        return AchievementSummary(
            totalUnlocked = unlocked,
            totalBadges = achievements.size,
            currentStreak = stats.streakDays,
            sessionsCompleted = stats.totalSessions
        )
    }

    private suspend fun buildStats(
        sessions: List<SessionEntity>,
        todayStepsOverride: Int?,
        stepsStreakOverride: Int?
    ): AchievementStats {
        val todaySteps = todayStepsOverride ?: runCatching {
            stepsRepository.getTodaySteps().toInt()
        }.getOrDefault(0)

        val stepsStreak = stepsStreakOverride ?: 0
        val totalSessions = sessions.size
        val sportsSessions = sessions.count { it.activityType == ActivityType.SPORT.name }
        val highEffortSports = sessions.count {
            it.activityType == ActivityType.SPORT.name && it.intensity == Intensity.HIGH.name
        }
        val streak = calculateStreak(sessions)

        return AchievementStats(
            streakDays = streak,
            totalSessions = totalSessions,
            sportsSessions = sportsSessions,
            highEffortSports = highEffortSports,
            todaySteps = todaySteps,
            stepsStreakDays = stepsStreak
        )
    }

    private fun AchievementEntity.toModel(): Achievement {
        return Achievement(
            id = id,
            title = title,
            description = description,
            type = AchievementType.valueOf(type.uppercase(Locale.getDefault())),
            threshold = threshold,
            progress = progress,
            unlocked = unlocked,
            unlockedAt = unlockedAt,
            icon = icon
        )
    }

    companion object {
        private val definitions = listOf(
            AchievementDefinition(
                id = "streak_3",
                title = "3-Day Streak",
                description = "Train 3 days in a row",
                type = AchievementType.STREAK,
                threshold = 3,
                icon = "🔥",
                progressSelector = { it.streakDays }
            ),
            AchievementDefinition(
                id = "streak_7",
                title = "7-Day Streak",
                description = "A full week of consistency",
                type = AchievementType.STREAK,
                threshold = 7,
                icon = "⚡",
                progressSelector = { it.streakDays }
            ),
            AchievementDefinition(
                id = "streak_14",
                title = "14-Day Streak",
                description = "Two weeks of momentum",
                type = AchievementType.STREAK,
                threshold = 14,
                icon = "🔥",
                progressSelector = { it.streakDays }
            ),
            AchievementDefinition(
                id = "streak_30",
                title = "30-Day Streak",
                description = "A month without missing",
                type = AchievementType.STREAK,
                threshold = 30,
                icon = "🏆",
                progressSelector = { it.streakDays }
            ),
            AchievementDefinition(
                id = "streak_60",
                title = "60-Day Streak",
                description = "Two months of hustle",
                type = AchievementType.STREAK,
                threshold = 60,
                icon = "💎",
                progressSelector = { it.streakDays }
            ),
            AchievementDefinition(
                id = "streak_100",
                title = "100-Day Streak",
                description = "Elite consistency",
                type = AchievementType.STREAK,
                threshold = 100,
                icon = "👑",
                progressSelector = { it.streakDays }
            ),
            AchievementDefinition(
                id = "sessions_1",
                title = "First Session",
                description = "Log your first workout",
                type = AchievementType.SESSIONS,
                threshold = 1,
                icon = "✅",
                progressSelector = { it.totalSessions }
            ),
            AchievementDefinition(
                id = "sessions_5",
                title = "5 Sessions",
                description = "Keep showing up",
                type = AchievementType.SESSIONS,
                threshold = 5,
                icon = "🎯",
                progressSelector = { it.totalSessions }
            ),
            AchievementDefinition(
                id = "sessions_10",
                title = "10 Sessions",
                description = "Double digits",
                type = AchievementType.SESSIONS,
                threshold = 10,
                icon = "💪",
                progressSelector = { it.totalSessions }
            ),
            AchievementDefinition(
                id = "sessions_25",
                title = "25 Sessions",
                description = "Quarter century grind",
                type = AchievementType.SESSIONS,
                threshold = 25,
                icon = "🚀",
                progressSelector = { it.totalSessions }
            ),
            AchievementDefinition(
                id = "sessions_50",
                title = "50 Sessions",
                description = "Halfway to 100",
                type = AchievementType.SESSIONS,
                threshold = 50,
                icon = "🏅",
                progressSelector = { it.totalSessions }
            ),
            AchievementDefinition(
                id = "sessions_100",
                title = "100 Sessions",
                description = "Century club",
                type = AchievementType.SESSIONS,
                threshold = 100,
                icon = "🏆",
                progressSelector = { it.totalSessions }
            ),
            AchievementDefinition(
                id = "sports_1",
                title = "First Sports Session",
                description = "Log your first sport workout",
                type = AchievementType.SPORTS,
                threshold = 1,
                icon = "⚽",
                progressSelector = { it.sportsSessions }
            ),
            AchievementDefinition(
                id = "sports_5",
                title = "5 Sports Sessions",
                description = "Build your sports habit",
                type = AchievementType.SPORTS,
                threshold = 5,
                icon = "🏀",
                progressSelector = { it.sportsSessions }
            ),
            AchievementDefinition(
                id = "sports_20",
                title = "20 Sports Sessions",
                description = "Serious sports momentum",
                type = AchievementType.SPORTS,
                threshold = 20,
                icon = "🏉",
                progressSelector = { it.sportsSessions }
            ),
            AchievementDefinition(
                id = "sports_beast_10",
                title = "High Effort Beast",
                description = "10 high-effort sports sessions",
                type = AchievementType.SPORTS,
                threshold = 10,
                icon = "🦍",
                progressSelector = { it.highEffortSports }
            ),
            AchievementDefinition(
                id = "steps_5000",
                title = "5,000 Steps",
                description = "Hit 5k steps in a day",
                type = AchievementType.STEPS,
                threshold = 5000,
                icon = "👟",
                progressSelector = { it.todaySteps }
            ),
            AchievementDefinition(
                id = "steps_10000",
                title = "10,000 Steps",
                description = "Hit 10k steps in a day",
                type = AchievementType.STEPS,
                threshold = 10000,
                icon = "🥾",
                progressSelector = { it.todaySteps }
            ),
            AchievementDefinition(
                id = "steps_15000",
                title = "15,000 Steps",
                description = "Hit 15k steps in a day",
                type = AchievementType.STEPS,
                threshold = 15000,
                icon = "🚶",
                progressSelector = { it.todaySteps }
            ),
            AchievementDefinition(
                id = "steps_streak_7",
                title = "7-Day Step Streak",
                description = "7 days over 7k steps",
                type = AchievementType.STEPS,
                threshold = 7,
                icon = "📈",
                progressSelector = { it.stepsStreakDays }
            )
        )
    }
}
