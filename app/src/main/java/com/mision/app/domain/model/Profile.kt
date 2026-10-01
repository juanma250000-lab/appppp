package com.mision.app.domain.model

import com.mision.app.core.gamification.LevelCalculator
import com.mision.app.core.gamification.LevelProgress

/**
 * Player profile. Level fields are derived from [totalXp] through
 * [LevelCalculator] so there is a single source of truth for progression.
 */
data class UserProfile(
    val name: String,
    val totalXp: Int,
    val coins: Int,
    val totalMissionsCompleted: Int,
    val createdAtEpochDay: Int,
) {
    val level: Int get() = LevelCalculator.levelFor(totalXp)
    val levelProgress: LevelProgress get() = LevelCalculator.progressFor(totalXp)

    companion object {
        fun empty(name: String = "", createdAtEpochDay: Int = 0) = UserProfile(
            name = name,
            totalXp = 0,
            coins = 0,
            totalMissionsCompleted = 0,
            createdAtEpochDay = createdAtEpochDay,
        )
    }
}

/** Daily aggregate used by the home screen and the statistics chart. */
data class DailyLog(
    val epochDay: Int,
    val completedCount: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    /** Missions planned that day, so completion ratio is known historically. */
    val totalMissions: Int = 0,
) {
    val isPerfectDay: Boolean
        get() = totalMissions > 0 && completedCount >= totalMissions

    companion object {
        fun empty(epochDay: Int) = DailyLog(epochDay, 0, 0, 0, 0)
    }
}

/** Snapshot of everything the profile screen needs. */
data class ProfileStats(
    val profile: UserProfile,
    val streak: StreakState,
    val unlockedAchievements: Int,
    val totalAchievements: Int,
    val weekStats: List<DayStat>,
    val monthCompleted: Int,
    val monthGoalDays: Int,
)

/** One day of the weekly chart. */
data class DayStat(
    val epochDay: Int,
    val completed: Int,
    val total: Int,
    val xpEarned: Int,
) {
    val progress: Float
        get() = if (total <= 0) 0f else (completed.toFloat() / total).coerceIn(0f, 1f)
}
