package com.mision.app.domain.usecase

import com.mision.app.core.gamification.AchievementCatalog
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.DayStat
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.ProfileStats
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.repository.TransactionRunner
import java.time.LocalDate

/**
 * Snapshot the profile screen needs: progression, achievements and the
 * weekly/monthly statistics.
 */
class GetProfileStatsUseCase(
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val missionRepository: MissionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(): ProfileStats {
        val logs = progressRepository.getDailyLogs()
        return ProfileStats(
            profile = progressRepository.getProfile(),
            streak = gamificationRepository.getStreak(),
            unlockedAchievements = gamificationRepository.getUnlockedAchievementIds().size,
            totalAchievements = AchievementCatalog.all.size,
            weekStats = weekStats(logs),
            monthCompleted = monthCompleted(logs),
            perfectDays = logs.count { it.isPerfectDay },
        )
    }

    /** Last seven days, oldest first, with the planned total of each day. */
    private suspend fun weekStats(logs: List<DailyLog>): List<DayStat> {
        val today = clock.today()
        val byDay = logs.associateBy { it.epochDay }
        return (DAYS_IN_WEEK - 1 downTo 0).map { daysAgo ->
            val epochDay = today.minusDays(daysAgo.toLong()).toEpochDay().toInt()
            val log = byDay[epochDay]
            DayStat(
                epochDay = epochDay,
                completed = log?.completedCount ?: 0,
                total = missionRepository.countForDay(epochDay).second,
                xpEarned = log?.xpEarned ?: 0,
            )
        }
    }

    private fun monthCompleted(logs: List<DailyLog>): Int {
        val today = clock.today()
        return logs
            .filter { log ->
                val day = LocalDate.ofEpochDay(log.epochDay.toLong())
                day.year == today.year && day.monthValue == today.monthValue
            }
            .sumOf { it.completedCount }
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}

/**
 * Persists the first-run choices (names and favourite categories) and marks
 * the onboarding as done. Blank names keep the defaults.
 */
class CompleteOnboardingUseCase(
    private val progressRepository: ProgressRepository,
    private val petRepository: PetRepository,
    private val settingsRepository: SettingsRepository,
    private val transaction: TransactionRunner,
) {
    suspend operator fun invoke(
        userName: String,
        petName: String,
        categories: Set<MissionCategory>,
    ) {
        transaction {
            val cleanUserName = userName.trim().take(NAME_MAX_LENGTH)
            if (cleanUserName.isNotEmpty()) {
                progressRepository.saveProfile(progressRepository.getProfile().copy(name = cleanUserName))
            }
            val pet = petRepository.getPet()
            petRepository.savePet(pet.copy(name = petName.trim().take(NAME_MAX_LENGTH).ifEmpty { pet.name }))
        }
        settingsRepository.setPreferredCategories(categories)
        settingsRepository.setOnboardingCompleted(true)
    }
}
