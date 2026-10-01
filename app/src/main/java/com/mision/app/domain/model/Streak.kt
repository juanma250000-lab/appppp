package com.mision.app.domain.model

/**
 * Immutable streak state.
 *
 * @property lastCompletedEpochDay last day the daily objective was met (-1 = never)
 * @property isActive false when the chain is broken because a day was skipped
 */
data class StreakState(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedEpochDay: Int,
    val totalActiveDays: Int,
    val isActive: Boolean,
) {
    val hasMilestone: Boolean get() = currentStreak >= 1

    companion object {
        const val NEVER = -1

        fun empty() = StreakState(
            currentStreak = 0,
            longestStreak = 0,
            lastCompletedEpochDay = NEVER,
            totalActiveDays = 0,
            isActive = false,
        )
    }
}

/** Result of registering a completed day, used to drive celebrations. */
data class StreakUpdate(
    val state: StreakState,
    val isNewRecord: Boolean,
    val milestone: StreakMilestone?,
    val daysGained: Int,
)

/** Rewarding streak milestones. */
data class StreakMilestone(
    val days: Int,
    val rewardXp: Int,
    val rewardCoins: Int,
    val title: String,
    val message: String,
)
