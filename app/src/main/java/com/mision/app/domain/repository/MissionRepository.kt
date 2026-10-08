package com.mision.app.domain.repository

import com.mision.app.domain.model.Mission
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import kotlinx.coroutines.flow.Flow

/**
 * Data needed to create or update a custom mission.
 */
data class MissionDraft(
    val title: String,
    val description: String = "",
    val category: MissionCategory,
    val difficulty: MissionDifficulty,
    val durationMinutes: Int? = null,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val isRecurring: Boolean = true,
)

/** An active mission with its own reminder time, ready to be scheduled. */
data class MissionReminder(
    val templateId: String,
    val title: String,
    val hour: Int,
    val minute: Int,
)

/**
 * Contract for mission templates (blueprints) and their daily instances.
 *
 * Instances are identified by `"$templateId:$epochDay"` so a recurring mission
 * has exactly one instance per calendar day.
 */
interface MissionRepository {

    /** Missions of [epochDay], pending first, ordered by sort order. */
    fun observeMissionsForDay(epochDay: Int): Flow<List<Mission>>

    suspend fun getMission(id: String): Mission?

    /**
     * Materialises today's instances from the active templates (seeding the
     * starter missions on first launch).
     */
    suspend fun ensureDailyMissions(todayEpochDay: Int)

    /** Marks an instance completed/pending; returns the updated mission. */
    suspend fun setCompleted(id: String, completed: Boolean, atEpochSecond: Long): Mission?

    suspend fun addCustomMission(draft: MissionDraft, todayEpochDay: Int): Mission

    /** Only custom missions can be edited; built-in ones return null. */
    suspend fun updateCustomMission(id: String, draft: MissionDraft): Mission?

    /** Only custom missions can be deleted; built-in ones return false. */
    suspend fun deleteCustomMission(id: String): Boolean

    suspend fun pendingCountToday(todayEpochDay: Int): Int

    /** Pending and total missions of a day, in that order. */
    suspend fun countForDay(epochDay: Int): Pair<Int, Int>

    suspend fun pruneOldInstances(beforeEpochDay: Int)

    /** Active missions whose reminder is switched on. */
    suspend fun remindersToSchedule(): List<MissionReminder>
}
