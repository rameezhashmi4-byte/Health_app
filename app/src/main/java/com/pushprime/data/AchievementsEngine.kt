package com.pushprime.data

import com.pushprime.model.Achievement
import com.pushprime.model.AchievementList
import com.pushprime.model.AchievementProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementsEngine @Inject constructor(
    private val localStore: LocalStore,
    private val analyticsHelper: AnalyticsHelper
) {
    private val _unlockedAchievementIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedAchievementIds: StateFlow<Set<String>> = _unlockedAchievementIds.asStateFlow()

    init {
        _unlockedAchievementIds.value = localStore.getUnlockedAchievements()
    }

    suspend fun checkAchievements(progress: AchievementProgress): List<Achievement> {
        val currentUnlocked = _unlockedAchievementIds.value
        val newlyUnlocked = mutableListOf<Achievement>()

        AchievementList.ACHIEVEMENTS.forEach { achievement ->
            if (!currentUnlocked.contains(achievement.id) && achievement.condition(progress)) {
                unlockAchievement(achievement)
                newlyUnlocked.add(achievement)
            }
        }

        return newlyUnlocked
    }

    private suspend fun unlockAchievement(achievement: Achievement) {
        val newSet = _unlockedAchievementIds.value + achievement.id
        _unlockedAchievementIds.value = newSet
        localStore.saveUnlockedAchievement(achievement.id)
        
        analyticsHelper.trackEvent(
            AnalyticsHelper.Events.ACHIEVEMENT_UNLOCKED,
            mapOf(AnalyticsHelper.Params.ACHIEVEMENT_ID to achievement.id)
        )
    }

    fun getAllAchievementsWithStatus(): List<Achievement> {
        val unlockedIds = _unlockedAchievementIds.value
        return AchievementList.ACHIEVEMENTS.map { achievement ->
            achievement.copy(isUnlocked = unlockedIds.contains(achievement.id))
        }
    }
}
