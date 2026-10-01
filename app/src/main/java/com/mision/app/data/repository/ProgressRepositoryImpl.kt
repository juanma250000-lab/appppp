package com.mision.app.data.repository

import com.mision.app.core.time.ClockProvider
import com.mision.app.data.local.ProgressDao
import com.mision.app.data.local.ProfileEntity
import com.mision.app.data.toDomain
import com.mision.app.data.toEntity
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressRepositoryImpl(
    private val dao: ProgressDao,
    private val clock: ClockProvider,
) : ProgressRepository {

    override fun observeProfile(): Flow<UserProfile> =
        dao.observeProfile().map { (it ?: defaultProfile()).toDomain() }

    override suspend fun getProfile(): UserProfile =
        (dao.getProfile() ?: defaultProfile()).toDomain()

    override suspend fun saveProfile(profile: UserProfile) {
        dao.upsertProfile(profile.toEntity())
    }

    override fun observeDailyLogs(): Flow<List<DailyLog>> =
        dao.observeDailyLogs().map { logs -> logs.map { it.toDomain() } }

    override suspend fun getDailyLog(epochDay: Int): DailyLog? =
        dao.getDailyLog(epochDay)?.toDomain()

    override suspend fun saveDailyLog(log: DailyLog) {
        dao.upsertDailyLog(log.toEntity())
    }

    override suspend fun totalCoinsEarned(): Int =
        dao.getAllDailyLogs().sumOf { it.coinsEarned }

    override suspend fun resetProgress() {
        dao.clearMissionInstances()
        dao.clearAchievements()
        dao.clearPurchases()
        dao.clearDailyLogs()

        val today = clock.todayEpochDay()
        val existing = dao.getProfile()
        dao.upsertProfile(
            ProfileEntity(
                id = ProfileEntity.PROFILE_ID,
                name = existing?.name ?: "Amigo",
                totalXp = 0,
                coins = 0,
                totalMissionsCompleted = 0,
                createdAtEpochDay = today,
            ),
        )
    }

    private fun defaultProfile() = ProfileEntity(
        id = ProfileEntity.PROFILE_ID,
        name = "Amigo",
        totalXp = 0,
        coins = 0,
        totalMissionsCompleted = 0,
        createdAtEpochDay = clock.todayEpochDay(),
    )
}
