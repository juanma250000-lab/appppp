package com.mision.app.domain.usecase

import com.mision.app.core.gamification.StreakCalculator
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.AchievementDefinition
import com.mision.app.domain.model.AppResult
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.Mission
import com.mision.app.domain.model.StreakMilestone
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.StreakUpdate
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionDraft
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow

/** Everything that changed when a mission was completed, for celebrations. */
data class MissionCompletionResult(
    val mission: Mission,
    /** Total XP added (mission + streak milestone + achievements). */
    val xpGained: Int,
    /** Total coins added (mission + streak milestone + achievements). */
    val coinsGained: Int,
    /** XP that came from streak milestones and achievements. */
    val bonusXp: Int,
    /** Coins that came from streak milestones and achievements. */
    val bonusCoins: Int,
    val previousLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val newAchievements: List<AchievementDefinition>,
    val streakUpdate: StreakUpdate,
    val perfectDay: Boolean,
    val completedToday: Int,
    val totalToday: Int,
) {
    val remainingToday: Int get() = (totalToday - completedToday).coerceAtLeast(0)
    val milestone: StreakMilestone? get() = streakUpdate.milestone
    val isNewStreakRecord: Boolean get() = streakUpdate.isNewRecord
    val hasCelebration: Boolean
        get() = leveledUp || newAchievements.isNotEmpty() || milestone != null || perfectDay
}

/**
 * Single entry point for completing a mission.
 *
 * Orchestrates every affected aggregate in one call: mission instance,
 * profile progression, daily statistics, streak, pet reaction and
 * achievements, so the UI never has to (and cannot forget to) update one of
 * them.
 */
class CompleteMissionUseCase(
    private val missionRepository: MissionRepository,
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val shopRepository: ShopRepository,
    private val petRepository: PetRepository,
    private val clock: ClockProvider,
) {

    suspend operator fun invoke(instanceId: String): MissionCompletionResult? {
        val mission = missionRepository.getMission(instanceId) ?: return null
        val today = clock.todayEpochDay()
        // Only missions of the current day can be completed: a stale instance
        // from a previous day must not silently corrupt today's statistics.
        if (mission.isCompleted || mission.dueEpochDay != today) return null

        val updated = missionRepository.setCompleted(
            id = instanceId,
            completed = true,
            atEpochSecond = clock.nowEpochSecond(),
        ) ?: return null

        // --- Streak (registered first: it can only grow on completion) ------
        val streakBefore = gamificationRepository.getStreak()
        val streakUpdate = StreakCalculator.registerCompletedDay(streakBefore, today)
        gamificationRepository.saveStreak(streakUpdate.state)

        // --- Profile progression -------------------------------------------
        val profileBefore = progressRepository.getProfile()
        var xpGained = updated.xpReward
        var coinsGained = updated.coinReward
        var bonusXp = 0
        var bonusCoins = 0

        streakUpdate.milestone?.let { milestone ->
            xpGained += milestone.rewardXp
            coinsGained += milestone.rewardCoins
            bonusXp += milestone.rewardXp
            bonusCoins += milestone.rewardCoins
        }

        var profile = profileBefore.copy(
            totalXp = profileBefore.totalXp + xpGained,
            coins = profileBefore.coins + coinsGained,
            totalMissionsCompleted = profileBefore.totalMissionsCompleted + 1,
        )
        progressRepository.saveProfile(profile)

        // --- Today's statistics --------------------------------------------
        val (pending, total) = missionRepository.countForDay(today)
        val completedToday = (total - pending).coerceIn(0, total)
        val perfectDay = total > 0 && pending == 0

        val logBefore = progressRepository.getDailyLog(today) ?: DailyLog.empty(today)
        var log = logBefore.copy(
            completedCount = completedToday,
            totalMissions = total,
        )

        // --- Pet reaction ----------------------------------------------------
        val ratio = if (total <= 0) 0f else completedToday.toFloat() / total
        PetEffects.onMissionCompleted(
            petRepository = petRepository,
            todayEpochDay = today,
            completionRatio = ratio,
            allCompleted = perfectDay,
            newStreakRecord = streakUpdate.isNewRecord,
            xpGained = xpGained,
        )

        // --- Achievements (evaluated after everything else is settled) ------
        val newAchievements = AchievementEffects.newlyUnlocked(
            progressRepository = progressRepository,
            gamificationRepository = gamificationRepository,
            shopRepository = shopRepository,
            clock = clock,
        )
        if (newAchievements.isNotEmpty()) {
            val achievementXp = newAchievements.sumOf { it.rewardXp }
            val achievementCoins = newAchievements.sumOf { it.rewardCoins }
            if (achievementXp > 0 || achievementCoins > 0) {
                profile = profile.copy(
                    totalXp = profile.totalXp + achievementXp,
                    coins = profile.coins + achievementCoins,
                )
                progressRepository.saveProfile(profile)
                xpGained += achievementXp
                coinsGained += achievementCoins
                bonusXp += achievementXp
                bonusCoins += achievementCoins
            }
        }

        log = log.copy(
            xpEarned = logBefore.xpEarned + xpGained,
            coinsEarned = logBefore.coinsEarned + coinsGained,
        )
        progressRepository.saveDailyLog(log)

        return MissionCompletionResult(
            mission = updated,
            xpGained = xpGained,
            coinsGained = coinsGained,
            bonusXp = bonusXp,
            bonusCoins = bonusCoins,
            previousLevel = profileBefore.level,
            newLevel = profile.level,
            leveledUp = profile.level > profileBefore.level,
            newAchievements = newAchievements,
            streakUpdate = streakUpdate,
            perfectDay = perfectDay,
            completedToday = completedToday,
            totalToday = total,
        )
    }
}

/**
 * Reverts a completion (only allowed for today's missions). Rewards earned by
 * the mission are removed; milestone and achievement rewards already granted
 * are intentionally kept.
 */
class UncompleteMissionUseCase(
    private val missionRepository: MissionRepository,
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val petRepository: PetRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(instanceId: String): Boolean {
        val mission = missionRepository.getMission(instanceId) ?: return false
        val today = clock.todayEpochDay()
        if (!mission.canBeUncompleted(today)) return false

        missionRepository.setCompleted(id = instanceId, completed = false, atEpochSecond = 0L)
            ?: return false

        val profile = progressRepository.getProfile()
        progressRepository.saveProfile(
            profile.copy(
                totalXp = (profile.totalXp - mission.xpReward).coerceAtLeast(0),
                coins = (profile.coins - mission.coinReward).coerceAtLeast(0),
                totalMissionsCompleted = (profile.totalMissionsCompleted - 1).coerceAtLeast(0),
            ),
        )

        val (pending, total) = missionRepository.countForDay(today)
        val completedToday = (total - pending).coerceIn(0, total)
        val log = progressRepository.getDailyLog(today)
        if (log != null) {
            progressRepository.saveDailyLog(
                log.copy(
                    completedCount = completedToday,
                    totalMissions = total,
                    xpEarned = (log.xpEarned - mission.xpReward).coerceAtLeast(0),
                    coinsEarned = (log.coinsEarned - mission.coinReward).coerceAtLeast(0),
                ),
            )
        }

        gamificationRepository.saveStreak(
            StreakCalculator.revertCompletedDay(gamificationRepository.getStreak(), today),
        )

        val ratio = if (total <= 0) 0f else completedToday.toFloat() / total
        PetEffects.refresh(
            petRepository = petRepository,
            todayEpochDay = today,
            completionRatio = ratio,
            allCompleted = total > 0 && pending == 0,
        )
        return true
    }
}

/** Live list of the missions of a given day, already sorted for the UI. */
class GetDailyMissionsUseCase(
    private val missionRepository: MissionRepository,
) {
    operator fun invoke(epochDay: Int): Flow<List<Mission>> =
        missionRepository.observeMissionsForDay(epochDay)
}

/** Creates today's instances from the templates (seeding on first launch). */
class EnsureDailyMissionsUseCase(
    private val missionRepository: MissionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke() = missionRepository.ensureDailyMissions(clock.todayEpochDay())
}

/**
 * Refreshed streak for display: a chain broken by a missed day reads as zero
 * (without destroying the historical record) and is persisted so every screen
 * agrees.
 */
class CalculateStreakUseCase(
    private val gamificationRepository: GamificationRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(): StreakState {
        val stored = gamificationRepository.getStreak()
        val effective = StreakCalculator.effectiveState(stored, clock.todayEpochDay())
        if (effective.currentStreak != stored.currentStreak ||
            effective.isActive != stored.isActive
        ) {
            gamificationRepository.saveStreak(effective)
        }
        return effective
    }
}

/** Creates a custom mission for today (and for future days if recurring). */
class CreateCustomMissionUseCase(
    private val missionRepository: MissionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(draft: MissionDraft): AppResult<Mission> {
        if (draft.title.isBlank()) {
            return AppResult.Error("Escribe un nombre para la misión.")
        }
        val mission = missionRepository.addCustomMission(draft, clock.todayEpochDay())
        return AppResult.Success(mission)
    }
}

/** Updates a custom mission template (built-in missions are read-only). */
class UpdateCustomMissionUseCase(
    private val missionRepository: MissionRepository,
) {
    suspend operator fun invoke(templateId: String, draft: MissionDraft): AppResult<Mission> {
        if (draft.title.isBlank()) {
            return AppResult.Error("Escribe un nombre para la misión.")
        }
        val updated = missionRepository.updateCustomMission(templateId, draft)
            ?: return AppResult.Error("Esta misión no se puede editar. Crea una nueva si lo necesitas.")
        return AppResult.Success(updated)
    }
}

/** Deletes a custom mission (and all its future instances). */
class DeleteCustomMissionUseCase(
    private val missionRepository: MissionRepository,
) {
    suspend operator fun invoke(templateId: String): Boolean =
        missionRepository.deleteCustomMission(templateId)
}
