package com.mision.app.domain.usecase

import com.mision.app.core.gamification.LevelCalculator
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.SettingsRepository

/** Outcome of adding experience, so the UI can celebrate a level-up. */
data class ExperienceResult(
    val profile: UserProfile,
    val previousLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val atMaxLevel: Boolean,
)

/**
 * Adds XP (and optionally coins) to the profile, clamping so no amount can
 * push the player below zero or above the maximum level.
 */
class AddExperienceUseCase(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(xp: Int, coins: Int = 0): ExperienceResult {
        val before = progressRepository.getProfile()
        val after = before.copy(
            totalXp = (before.totalXp + xp).coerceAtLeast(0),
            coins = (before.coins + coins).coerceAtLeast(0),
        )
        progressRepository.saveProfile(after)
        return ExperienceResult(
            profile = after,
            previousLevel = before.level,
            newLevel = after.level,
            leveledUp = after.level > before.level,
            atMaxLevel = after.level >= LevelCalculator.MAX_LEVEL,
        )
    }
}

/**
 * Explicit level-up check used after big rewards (milestones, achievements):
 * compare the XP before an action with the stored profile to detect the
 * transition.
 */
class LevelUpUseCase(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(xpBefore: Int): ExperienceResult {
        val profile = progressRepository.getProfile()
        val previousLevel = LevelCalculator.levelFor(xpBefore)
        return ExperienceResult(
            profile = profile,
            previousLevel = previousLevel,
            newLevel = profile.level,
            leveledUp = profile.level > previousLevel,
            atMaxLevel = profile.level >= LevelCalculator.MAX_LEVEL,
        )
    }

    /** XP still missing to reach the next level (0 at max level). */
    suspend fun xpMissingForNextLevel(): Int {
        val profile = progressRepository.getProfile()
        if (profile.level >= LevelCalculator.MAX_LEVEL) return 0
        return (LevelCalculator.xpRequiredForLevel(profile.level + 1) - profile.totalXp)
            .coerceAtLeast(0)
    }
}

/**
 * Restores the app to a brand-new state: progression, pet, streak,
 * achievements, purchases and daily instances. Settings are preserved so the
 * user does not lose their preferences.
 */
class ResetProgressUseCase(
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val petRepository: PetRepository,
    private val missionRepository: MissionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke() {
        val petName = petRepository.getPet().name
        progressRepository.resetProgress()
        gamificationRepository.saveStreak(StreakState.empty())
        petRepository.resetPet(petName)
        missionRepository.ensureDailyMissions(clock.todayEpochDay())
    }
}

/** Renames the player, trimming and clamping the input. */
class UpdateProfileNameUseCase(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(rawName: String): UserProfile {
        val cleanName = rawName.trim().take(MAX_LENGTH)
        if (cleanName.isEmpty()) return progressRepository.getProfile()
        val profile = progressRepository.getProfile()
        val updated = profile.copy(name = cleanName)
        progressRepository.saveProfile(updated)
        return updated
    }

    companion object {
        const val MAX_LENGTH = 24
    }
}

/** Persists the categories chosen during onboarding or in settings. */
class SetPreferredCategoriesUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(categories: Set<MissionCategory>) =
        settingsRepository.setPreferredCategories(categories)
}

/** Daily-log helper shared by statistics screens. */
class GetDailyLogUseCase(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(epochDay: Int): DailyLog =
        progressRepository.getDailyLog(epochDay) ?: DailyLog.empty(epochDay)
}
