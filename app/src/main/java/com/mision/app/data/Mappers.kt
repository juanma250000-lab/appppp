package com.mision.app.data

import com.mision.app.data.local.DailyLogEntity
import com.mision.app.data.local.MissionInstanceEntity
import com.mision.app.data.local.MissionTemplateEntity
import com.mision.app.data.local.PetEntity
import com.mision.app.data.local.ProfileEntity
import com.mision.app.data.local.StreakEntity
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Mission
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.UserProfile

/** Entity <-> domain mappings. All conversion between layers happens here. */

fun MissionTemplateEntity.toMission(instance: MissionInstanceEntity): Mission = Mission(
    id = instance.id,
    templateId = id,
    title = title,
    description = description,
    category = enumValueOr(category, MissionCategory.PERSONAL),
    difficulty = enumValueOr(difficulty, MissionDifficulty.FACIL),
    xpReward = xpReward,
    coinReward = coinReward,
    isCompleted = instance.isCompleted,
    completedAtEpochSecond = instance.completedAtEpochSecond,
    dueEpochDay = instance.dueEpochDay,
    createdAtEpochDay = createdAtEpochDay,
    isRecurring = isRecurring,
    isCustom = isCustom,
    reminderEnabled = reminderEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    durationMinutes = durationMinutes.takeIf { it > 0 },
    sortOrder = sortOrder,
)

fun ProfileEntity.toDomain(): UserProfile = UserProfile(
    name = name,
    totalXp = totalXp,
    coins = coins,
    totalMissionsCompleted = totalMissionsCompleted,
    createdAtEpochDay = createdAtEpochDay,
)

fun UserProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = ProfileEntity.PROFILE_ID,
    name = name.ifBlank { "Amigo" },
    totalXp = totalXp.coerceAtLeast(0),
    coins = coins.coerceAtLeast(0),
    totalMissionsCompleted = totalMissionsCompleted.coerceAtLeast(0),
    createdAtEpochDay = createdAtEpochDay,
)

fun PetEntity.toDomain(): Pet = Pet(
    name = name,
    xp = xp,
    happiness = happiness.coerceIn(0, 100),
    energy = energy.coerceIn(0, 100),
    mood = enumValueOr(mood, PetMood.MOTIVADO),
    equipped = EquippedCosmetics.decode(equippedCosmetics),
    lastInteractionEpochDay = lastInteractionEpochDay,
)

fun Pet.toEntity(): PetEntity = PetEntity(
    id = PetEntity.PET_ID,
    name = name.ifBlank { "Nube" },
    xp = xp.coerceAtLeast(0),
    happiness = happiness.coerceIn(0, 100),
    energy = energy.coerceIn(0, 100),
    mood = mood.name,
    equippedCosmetics = equipped.encode(),
    lastInteractionEpochDay = lastInteractionEpochDay,
    createdAtEpochDay = lastInteractionEpochDay,
)

fun StreakEntity.toDomain(isActive: Boolean): StreakState = StreakState(
    currentStreak = currentStreak.coerceAtLeast(0),
    longestStreak = longestStreak.coerceAtLeast(0),
    lastCompletedEpochDay = lastCompletedEpochDay,
    totalActiveDays = totalActiveDays.coerceAtLeast(0),
    isActive = isActive,
)

fun StreakState.toEntity(): StreakEntity = StreakEntity(
    id = StreakEntity.STREAK_ID,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastCompletedEpochDay = lastCompletedEpochDay,
    totalActiveDays = totalActiveDays,
)

fun DailyLogEntity.toDomain(): DailyLog = DailyLog(
    epochDay = epochDay,
    completedCount = completedCount,
    xpEarned = xpEarned,
    coinsEarned = coinsEarned,
    totalMissions = totalMissions,
)

fun DailyLog.toEntity(): DailyLogEntity = DailyLogEntity(
    epochDay = epochDay,
    completedCount = completedCount,
    totalMissions = maxOf(totalMissions, completedCount),
    xpEarned = xpEarned,
    coinsEarned = coinsEarned,
)

inline fun <reified T : Enum<T>> enumValueOr(raw: String, fallback: T): T =
    runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
