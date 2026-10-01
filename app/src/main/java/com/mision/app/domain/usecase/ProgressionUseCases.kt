package com.mision.app.domain.usecase

import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.TransactionRunner

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
    private val transaction: TransactionRunner,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke() = transaction {
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
        val cleanName = rawName.trim().take(NAME_MAX_LENGTH)
        val profile = progressRepository.getProfile()
        if (cleanName.isEmpty()) return profile
        val updated = profile.copy(name = cleanName)
        progressRepository.saveProfile(updated)
        return updated
    }
}

/** Maximum length of the player and pet names. */
const val NAME_MAX_LENGTH = 24
