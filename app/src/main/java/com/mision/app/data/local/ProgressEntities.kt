package com.mision.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single row table holding the player profile. */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = PROFILE_ID,
    val name: String,
    val totalXp: Int,
    val coins: Int,
    val totalMissionsCompleted: Int,
    val createdAtEpochDay: Int,
) {
    companion object {
        const val PROFILE_ID = 1
    }
}

/** Single row table holding the virtual pet state. */
@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey val id: Int = PET_ID,
    val name: String,
    val xp: Int,
    val happiness: Int,
    val energy: Int,
    val mood: String,
    /** Encoded "slot:itemId" pairs, e.g. "hat:hat_party;bg:bg_nebula". */
    val equippedCosmetics: String,
    val lastInteractionEpochDay: Int,
    val createdAtEpochDay: Int,
) {
    companion object {
        const val PET_ID = 1
    }
}

/** Single row table with the raw streak counters. */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val id: Int = STREAK_ID,
    val currentStreak: Int,
    val longestStreak: Int,
    /** -1 when the daily objective has never been completed. */
    val lastCompletedEpochDay: Int,
    val totalActiveDays: Int,
) {
    companion object {
        const val STREAK_ID = 1
        const val NEVER = -1
    }
}

/** Unlocked achievement. Rows only exist once unlocked. */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAtEpochSecond: Long,
)

/** Purchased shop item. Rows only exist once purchased. */
@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val itemId: String,
    val purchasedAtEpochSecond: Long,
)

/** Aggregated results of a single calendar day, used by the statistics chart. */
@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val epochDay: Int,
    val completedCount: Int,
    val totalMissions: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
)
