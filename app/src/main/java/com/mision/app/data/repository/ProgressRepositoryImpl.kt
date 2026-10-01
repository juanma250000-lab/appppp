package com.mision.app.data.repository

import com.mision.app.core.time.ClockProvider
import com.mision.app.data.local.ProgressDao
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
        dao.observeProfile().map { it?.toDomain() ?: defaultProfile() }

    override suspend fun getProfile(): UserProfile =
        dao.getProfile()?.toDomain() ?: defaultProfile()

    override suspend fun saveProfile(profile: UserProfile) {
        dao.upsertProfile(profile.toEntity())
    }

    override suspend fun getDailyLogs(): List<DailyLog> =
        dao.getAllDailyLogs().map { it.toDomain() }

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

        val name = dao.getProfile()?.name ?: UserProfile.DEFAULT_NAME
        dao.upsertProfile(
            UserProfile.empty(name = name, createdAtEpochDay = clock.todayEpochDay()).toEntity(),
        )
    }

    private fun defaultProfile() = UserProfile.empty(
        name = UserProfile.DEFAULT_NAME,
        createdAtEpochDay = clock.todayEpochDay(),
    )
}
