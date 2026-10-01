package com.mision.app.domain.usecase

import com.mision.app.core.gamification.AchievementCatalog
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.DayStat
import com.mision.app.domain.model.ProfileStats
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.first
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
        val profile = progressRepository.getProfile()
        val streak = gamificationRepository.getStreak()
        val unlocked = gamificationRepository.getUnlockedAchievementIds()
        val logs = progressRepository.observeDailyLogs().first()

        return ProfileStats(
            profile = profile,
            streak = streak,
            unlockedAchievements = unlocked.size,
            totalAchievements = AchievementCatalog.all.size,
            weekStats = weekStats(logs),
            monthCompleted = monthCompleted(logs),
            monthGoalDays = clock.today().dayOfMonth,
        )
    }

    /** Last seven days, oldest first, with the planned total of each day. */
    private suspend fun weekStats(logs: List<DailyLog>): List<DayStat> {
        val today = clock.today()
        val byDay = logs.associateBy { it.epochDay }
        return (0 until DAYS_IN_WEEK).map { offset ->
            val day: LocalDate = today.minusDays((DAYS_IN_WEEK - 1 - offset).toLong())
            val epochDay = day.toEpochDay().toInt()
            val log = byDay[epochDay]
            val total = missionRepository.countForDay(epochDay).second
            DayStat(
                epochDay = epochDay,
                completed = log?.completedCount ?: 0,
                total = total,
                xpEarned = log?.xpEarned ?: 0,
            )
        }
    }

    /** Missions completed during the current calendar month. */
    private fun monthCompleted(logs: List<DailyLog>): Int {
        val today = clock.today()
        return logs
            .filter { log ->
                val day = LocalDate.ofEpochDay(log.epochDay.toLong())
                day.year == today.year && day.monthValue == today.monthValue
            }
            .sumOf { it.completedCount }
    }

    companion object {
        private const val DAYS_IN_WEEK = 7
    }
}
