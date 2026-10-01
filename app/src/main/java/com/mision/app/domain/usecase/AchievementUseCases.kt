package com.mision.app.domain.usecase

import com.mision.app.core.gamification.AchievementCatalog
import com.mision.app.core.gamification.AchievementEvaluator
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.Achievement
import com.mision.app.domain.model.AchievementDefinition
import com.mision.app.domain.model.AchievementStats
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.ShopRepository

/**
 * Builds the immutable snapshot every achievement is evaluated from, so the
 * rules stay pure and unit testable.
 */
internal object AchievementEffects {

    suspend fun stats(
        progressRepository: ProgressRepository,
        gamificationRepository: GamificationRepository,
        shopRepository: ShopRepository,
    ): AchievementStats {
        val profile = progressRepository.getProfile()
        return AchievementStats(
            totalMissionsCompleted = profile.totalMissionsCompleted,
            longestStreak = gamificationRepository.getStreak().longestStreak,
            perfectDays = gamificationRepository.countPerfectDays(),
            level = profile.level,
            coinsEarned = progressRepository.totalCoinsEarned(),
            purchases = shopRepository.getPurchases().size,
        )
    }

    /** Newly unlocked achievements for the current snapshot (never re-fires). */
    suspend fun newlyUnlocked(
        progressRepository: ProgressRepository,
        gamificationRepository: GamificationRepository,
        shopRepository: ShopRepository,
        clock: ClockProvider,
    ): List<AchievementDefinition> {
        val unlocked = AchievementEvaluator.newlyUnlocked(
            stats = stats(progressRepository, gamificationRepository, shopRepository),
            alreadyUnlocked = gamificationRepository.getUnlockedAchievementIds(),
        )
        gamificationRepository.unlockAchievements(
            ids = unlocked.map { it.id },
            unlockedAtEpochSecond = clock.nowEpochSecond(),
        )
        return unlocked
    }
}

/** One achievement card: its unlock state plus the live progress of its counter. */
data class AchievementProgress(
    val achievement: Achievement,
    /** 0f..1f towards the target. */
    val progress: Float,
)

/**
 * Joins the static catalogue with the unlocked ids, so the UI receives ready
 * to render cards (locked ones include their live progress).
 */
class GetAchievementsUseCase(
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val shopRepository: ShopRepository,
) {
    suspend operator fun invoke(): List<AchievementProgress> {
        val stats = AchievementEffects.stats(progressRepository, gamificationRepository, shopRepository)
        val unlocked = gamificationRepository.getUnlockedAchievementIds()
        return AchievementCatalog.all.map { definition ->
            AchievementProgress(
                achievement = Achievement(
                    definition = definition,
                    isUnlocked = definition.id in unlocked,
                ),
                progress = definition.progressOf(stats),
            )
        }
    }
}
