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
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.repository.ShopRepository
import com.mision.app.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Everything that changed when a mission was completed, for celebrations. */
data class MissionCompletionResult(
    val mission: Mission,
    /** Total XP added (mission + streak milestone + achievements). */
    val xpGained: Int,
    /** Total coins added (mission + streak milestone + achievements). */
    val coinsGained: Int,
    /** XP that came from achievements (milestone XP is reported by [milestone]). */
    val achievementXp: Int,
    /** Coins that came from achievements (milestone coins are reported by [milestone]). */
    val achievementCoins: Int,
    val previousLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    val newAchievements: List<AchievementDefinition>,
    val streakUpdate: StreakUpdate,
    val perfectDay: Boolean,
    val completedToday: Int,
    val totalToday: Int,
) {
    val milestone: StreakMilestone? get() = streakUpdate.milestone
}

/**
 * Single entry point for completing a mission.
 *
 * Orchestrates every affected aggregate in one transaction: mission instance,
 * profile progression, daily statistics, streak, pet reaction and
 * achievements, so the UI never has to (and cannot forget to) update one of
 * them, and a double tap can never pay the same mission twice.
 */
class CompleteMissionUseCase(
    private val missionRepository: MissionRepository,
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val shopRepository: ShopRepository,
    private val petRepository: PetRepository,
    private val transaction: TransactionRunner,
    private val clock: ClockProvider,
) {

    suspend operator fun invoke(instanceId: String): MissionCompletionResult? = transaction {
        complete(instanceId)
    }

    private suspend fun complete(instanceId: String): MissionCompletionResult? {
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
        val streakUpdate = StreakCalculator.registerCompletedDay(gamificationRepository.getStreak(), today)
        gamificationRepository.saveStreak(streakUpdate.state)

        // --- Profile progression -------------------------------------------
        val profileBefore = progressRepository.getProfile()
        val milestone = streakUpdate.milestone
        var xpGained = updated.xpReward + (milestone?.rewardXp ?: 0)
        var coinsGained = updated.coinReward + (milestone?.rewardCoins ?: 0)

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
        // Saved before the achievements are evaluated: "perfect day" and
        // "coins earned" achievements read today's log and must see this mission.
        var log = logBefore.copy(
            completedCount = completedToday,
            totalMissions = total,
            xpEarned = logBefore.xpEarned + xpGained,
            coinsEarned = logBefore.coinsEarned + coinsGained,
        )
        progressRepository.saveDailyLog(log)

        // --- Pet reaction ----------------------------------------------------
        PetEffects.onMissionCompleted(
            petRepository = petRepository,
            todayEpochDay = today,
            completionRatio = if (total <= 0) 0f else completedToday.toFloat() / total,
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
            log = log.copy(
                xpEarned = log.xpEarned + achievementXp,
                coinsEarned = log.coinsEarned + achievementCoins,
            )
            progressRepository.saveDailyLog(log)
        }

        return MissionCompletionResult(
            mission = updated,
            xpGained = xpGained,
            coinsGained = coinsGained,
            achievementXp = achievementXp,
            achievementCoins = achievementCoins,
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
 * Reverts a completion (only allowed for today's missions).
 *
 * The mission's own rewards are removed. The streak only steps back once no
 * mission of the day remains completed, and the milestone paid by that day is
 * taken back with it: otherwise undoing and redoing the only mission of a
 * milestone day would pay the milestone again and again. Achievement rewards
 * already granted are kept, since achievements never unlock twice.
 */
class UncompleteMissionUseCase(
    private val missionRepository: MissionRepository,
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val petRepository: PetRepository,
    private val transaction: TransactionRunner,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(instanceId: String): Boolean = transaction {
        uncomplete(instanceId)
    }

    private suspend fun uncomplete(instanceId: String): Boolean {
        val mission = missionRepository.getMission(instanceId) ?: return false
        val today = clock.todayEpochDay()
        if (!mission.canBeUncompleted(today)) return false

        missionRepository.setCompleted(id = instanceId, completed = false, atEpochSecond = 0L)
            ?: return false

        val (pending, total) = missionRepository.countForDay(today)
        val completedToday = (total - pending).coerceIn(0, total)

        val milestone = if (completedToday == 0) revertStreak(today) else null
        val xpLost = mission.xpReward + (milestone?.rewardXp ?: 0)
        val coinsLost = mission.coinReward + (milestone?.rewardCoins ?: 0)

        val profile = progressRepository.getProfile()
        progressRepository.saveProfile(
            profile.copy(
                totalXp = (profile.totalXp - xpLost).coerceAtLeast(0),
                coins = (profile.coins - coinsLost).coerceAtLeast(0),
                totalMissionsCompleted = (profile.totalMissionsCompleted - 1).coerceAtLeast(0),
            ),
        )

        progressRepository.getDailyLog(today)?.let { log ->
            progressRepository.saveDailyLog(
                log.copy(
                    completedCount = completedToday,
                    totalMissions = total,
                    xpEarned = (log.xpEarned - xpLost).coerceAtLeast(0),
                    coinsEarned = (log.coinsEarned - coinsLost).coerceAtLeast(0),
                ),
            )
        }

        PetEffects.refresh(
            petRepository = petRepository,
            todayEpochDay = today,
            completionRatio = if (total <= 0) 0f else completedToday.toFloat() / total,
            allCompleted = total > 0 && pending == 0,
        )
        return true
    }

    /** Steps the streak back for [today]; returns the milestone that day had paid. */
    private suspend fun revertStreak(today: Int): StreakMilestone? {
        val before = gamificationRepository.getStreak()
        if (before.lastCompletedEpochDay != today) return null
        gamificationRepository.saveStreak(StreakCalculator.revertCompletedDay(before, today))
        return StreakCalculator.milestoneFor(before.currentStreak)
    }
}

/**
 * Live list of the missions of a given day, pending first and, among them,
 * the user's favourite categories first.
 */
class GetDailyMissionsUseCase(
    private val missionRepository: MissionRepository,
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(epochDay: Int): Flow<List<Mission>> = combine(
        missionRepository.observeMissionsForDay(epochDay),
        settingsRepository.settings.map { it.preferredCategories }.distinctUntilChanged(),
    ) { missions, favourites ->
        // sortedWith is stable, so the repository order is kept inside each group.
        missions.sortedWith(
            compareBy<Mission> { it.isCompleted }.thenBy { it.category !in favourites },
        )
    }
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
        validate(draft)?.let { return it }
        return AppResult.Success(missionRepository.addCustomMission(draft, clock.todayEpochDay()))
    }
}

/** Updates a custom mission template (built-in missions are read-only). */
class UpdateCustomMissionUseCase(
    private val missionRepository: MissionRepository,
) {
    suspend operator fun invoke(templateId: String, draft: MissionDraft): AppResult<Mission> {
        validate(draft)?.let { return it }
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

/** Maximum length of a mission title, enforced by the editor and the use cases. */
const val MISSION_TITLE_MAX_LENGTH = 60

private fun validate(draft: MissionDraft): AppResult.Error? = when {
    draft.title.isBlank() -> AppResult.Error("Escribe un nombre para la misión.")
    draft.title.trim().length > MISSION_TITLE_MAX_LENGTH ->
        AppResult.Error("El nombre puede tener como máximo $MISSION_TITLE_MAX_LENGTH caracteres.")
    else -> null
}
