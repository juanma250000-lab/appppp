package com.mision.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // ---- Profile ---------------------------------------------------------

    @Query("SELECT * FROM profile WHERE id = 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    // ---- Pet -------------------------------------------------------------

    @Query("SELECT * FROM pet WHERE id = 1")
    fun observePet(): Flow<PetEntity?>

    @Query("SELECT * FROM pet WHERE id = 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPet(pet: PetEntity)

    // ---- Streak ----------------------------------------------------------

    @Query("SELECT * FROM streak WHERE id = 1")
    fun observeStreak(): Flow<StreakEntity?>

    @Query("SELECT * FROM streak WHERE id = 1")
    suspend fun getStreak(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity)

    // ---- Achievements ----------------------------------------------------

    @Query("SELECT * FROM achievements")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievements")
    suspend fun getUnlockedAchievementIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    // ---- Shop ------------------------------------------------------------

    @Query("SELECT * FROM purchases")
    fun observePurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT itemId FROM purchases")
    suspend fun getPurchasedItemIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    // ---- Daily statistics ------------------------------------------------

    @Query("SELECT * FROM daily_logs")
    suspend fun getAllDailyLogs(): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE epochDay = :epochDay")
    suspend fun getDailyLog(epochDay: Int): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyLogEntity)

    // ---- Reset -----------------------------------------------------------

    @Query("DELETE FROM mission_instances")
    suspend fun clearMissionInstances()

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    @Query("DELETE FROM purchases")
    suspend fun clearPurchases()

    @Query("DELETE FROM daily_logs")
    suspend fun clearDailyLogs()
}
