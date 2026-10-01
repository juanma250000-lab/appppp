package com.mision.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Blueprint of a mission. Templates are what repeats day after day; the actual
 * daily checklist is materialised as [MissionInstanceEntity] rows so history
 * and current day never interfere with each other.
 */
@Entity(tableName = "mission_templates")
data class MissionTemplateEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val xpReward: Int,
    val coinReward: Int,
    /** When true a fresh instance is generated every day. */
    val isRecurring: Boolean,
    /** Custom missions can be edited/removed by the user. */
    val isCustom: Boolean,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    /** Optional planned duration in minutes. */
    val durationMinutes: Int,
    val sortOrder: Int,
    val isActive: Boolean,
    val createdAtEpochDay: Int,
)

/**
 * A concrete mission belonging to one calendar day (epoch day = proleptic
 * Julian day, which makes date maths deterministic across time zones).
 */
@Entity(
    tableName = "mission_instances",
    indices = [Index(value = ["dueEpochDay"]), Index(value = ["templateId", "dueEpochDay"], unique = true)],
)
data class MissionInstanceEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val dueEpochDay: Int,
    val isCompleted: Boolean,
    val completedAtEpochSecond: Long,
    val createdAtEpochDay: Int,
)
