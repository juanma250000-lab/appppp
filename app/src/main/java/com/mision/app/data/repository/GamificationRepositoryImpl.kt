package com.mision.app.data.repository

import com.mision.app.data.local.AchievementEntity
import com.mision.app.data.local.ProgressDao
import com.mision.app.data.local.StreakEntity
import com.mision.app.data.toDomain
import com.mision.app.data.toEntity
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GamificationRepositoryImpl(
    private val dao: ProgressDao,
) : GamificationRepository {

    override fun observeStreak(): Flow<StreakState> =
        dao.observeStreak().map { (it ?: defaultStreak()).toDomain(isActive = false) }

    override suspend fun getStreak(): StreakState =
        (dao.getStreak() ?: defaultStreak()).toDomain(isActive = false)

    override suspend fun saveStreak(state: StreakState) {
        dao.upsertStreak(state.toEntity())
    }

    override fun observeUnlockedAchievementIds(): Flow<Set<String>> =
        dao.observeAchievements().map { list -> list.map { it.id }.toSet() }

    override suspend fun getUnlockedAchievementIds(): Set<String> =
        dao.getUnlockedAchievementIds().toSet()

    override suspend fun unlockAchievements(ids: List<String>, unlockedAtEpochSecond: Long) {
        if (ids.isEmpty()) return
        dao.insertAchievements(
            ids.map { AchievementEntity(id = it, unlockedAtEpochSecond = unlockedAtEpochSecond) },
        )
    }

    override suspend fun countPerfectDays(): Int =
        dao.getAllDailyLogs().count { it.totalMissions > 0 && it.completedCount >= it.totalMissions }

    private fun defaultStreak() = StreakEntity(
        id = StreakEntity.STREAK_ID,
        currentStreak = 0,
        longestStreak = 0,
        lastCompletedEpochDay = StreakEntity.NEVER,
        totalActiveDays = 0,
    )
}
