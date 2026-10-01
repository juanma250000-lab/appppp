package com.mision.app.domain.repository

import com.mision.app.domain.model.AppSettings
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.ThemeMode
import com.mision.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** User profile: name, experience, coins and lifetime counters. */
interface ProgressRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun getProfile(): UserProfile
    suspend fun saveProfile(profile: UserProfile)

    fun observeDailyLogs(): Flow<List<DailyLog>>
    suspend fun getDailyLog(epochDay: Int): DailyLog?
    suspend fun saveDailyLog(log: DailyLog)

    /** Lifetime coins earned from missions, achievements and milestones. */
    suspend fun totalCoinsEarned(): Int

    /** Wipes progression (profile, missions, achievements, stats). */
    suspend fun resetProgress()
}

/** Streak (racha) persistence. */
interface GamificationStreakSource {
    fun observeStreak(): Flow<StreakState>
    suspend fun getStreak(): StreakState
    suspend fun saveStreak(state: StreakState)
}

/** Pet state and equipped cosmetics. */
interface PetRepository {
    fun observePet(): Flow<Pet>
    suspend fun getPet(): Pet
    suspend fun savePet(pet: Pet)
    suspend fun resetPet(name: String)
}

/**
 * Streak, unlocked achievements and perfect-day statistics.
 */
interface GamificationRepository : GamificationStreakSource {
    fun observeUnlockedAchievementIds(): Flow<Set<String>>
    suspend fun getUnlockedAchievementIds(): Set<String>
    suspend fun unlockAchievements(ids: List<String>, unlockedAtEpochSecond: Long)
    suspend fun countPerfectDays(): Int
}

/** Static catalogue, purchases and equipped cosmetics of the shop. */
interface ShopRepository {
    fun observeCatalog(): Flow<List<ShopItem>>
    fun observePurchases(): Flow<Set<String>>
    suspend fun getPurchases(): Set<String>
    suspend fun recordPurchase(itemId: String, atEpochSecond: Long)
    fun observeEquipped(): Flow<EquippedCosmetics>
    suspend fun equip(itemId: String)

    /** Pure decision helper: owned / too expensive / affordable. */
    suspend fun purchaseResult(item: ShopItem, currentCoins: Int): PurchaseResult
}

/** App settings (theme, reminders, onboarding...). */
interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(value: Boolean)
    suspend fun setNotificationsEnabled(value: Boolean)
    suspend fun setReminderTime(hour: Int, minute: Int)
    suspend fun setSoundEnabled(value: Boolean)
    suspend fun setAnimationsEnabled(value: Boolean)
    suspend fun setPreferredCategories(categories: Set<MissionCategory>)
    suspend fun markNotificationPermissionRequested()
}
