package com.mision.app.domain.model

/** Icon family used to render an achievement (mapped to Material icons in the UI). */
enum class AchievementIcon {
    TROPHY,
    FLAME,
    STAR,
    CHECK,
    BOOK,
    HEART,
    BOLT,
    MEDAL,
    CROWN,
    SPARKLE,
}

/** Why an achievement unlocks. Evaluated by [AchievementEvaluator]. */
enum class AchievementType {
    FIRST_MISSION,
    TOTAL_MISSIONS,
    STREAK_DAYS,
    PERFECT_DAY,
    LEVEL_REACHED,
    COINS_EARNED,
    SHOP_PURCHASES,
}

/** Static definition of an achievement (catalog entry). */
data class AchievementDefinition(
    val id: String,
    val name: String,
    val description: String,
    val icon: AchievementIcon,
    val type: AchievementType,
    val target: Int,
    val rewardXp: Int,
    val rewardCoins: Int,
) {
    /** Progress of the achievement for the given snapshot, 0f..1f. */
    fun progressOf(stats: AchievementStats): Float {
        val current = currentValue(stats)
        return if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)
    }

    fun currentValue(stats: AchievementStats): Int = when (type) {
        AchievementType.FIRST_MISSION -> if (stats.totalMissionsCompleted > 0) 1 else 0
        AchievementType.TOTAL_MISSIONS -> stats.totalMissionsCompleted
        AchievementType.STREAK_DAYS -> stats.longestStreak
        AchievementType.PERFECT_DAY -> stats.perfectDays
        AchievementType.LEVEL_REACHED -> stats.level
        AchievementType.COINS_EARNED -> stats.coinsEarned
        AchievementType.SHOP_PURCHASES -> stats.purchases
    }

    fun isUnlocked(stats: AchievementStats): Boolean =
        currentValue(stats) >= target
}

/** Facts required to evaluate every achievement. */
data class AchievementStats(
    val totalMissionsCompleted: Int,
    val longestStreak: Int,
    val perfectDays: Int,
    val level: Int,
    val coinsEarned: Int,
    val purchases: Int,
)

/** Achievement joined with its unlock state for the UI. */
data class Achievement(
    val definition: AchievementDefinition,
    val unlockedAtEpochSecond: Long?,
) {
    val isUnlocked: Boolean get() = unlockedAtEpochSecond != null
}
