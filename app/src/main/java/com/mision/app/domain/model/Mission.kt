package com.mision.app.domain.model

/** Broad areas a mission can belong to. */
enum class MissionCategory(val displayName: String) {
    SALUD("Salud"),
    ESTUDIO("Estudio"),
    PRODUCTIVIDAD("Productividad"),
    BIENESTAR("Bienestar"),
    ACTIVIDAD_FISICA("Actividad física"),
    PERSONAL("Personal"),
}

/**
 * Difficulty drives the reward table. Balancing happens here only: no screen
 * may decide how much XP or how many coins a mission pays.
 */
enum class MissionDifficulty(
    val displayName: String,
    val xpReward: Int,
    val coinReward: Int,
) {
    FACIL("Fácil", 10, 5),
    MEDIA("Media", 25, 10),
    DIFICIL("Difícil", 50, 20),
    EPICA("Épica", 100, 50),
}

enum class MissionStatus(val displayName: String) {
    PENDIENTE("Pendiente"),
    COMPLETADA("Completada"),
}

/**
 * Domain representation of one mission of a concrete day.
 *
 * @property templateId origin blueprint (used to keep one instance per day)
 * @property dueEpochDay calendar day the mission belongs to (proleptic Julian day)
 * @property completedAtEpochSecond wall-clock instant of completion (0 when pending)
 */
data class Mission(
    val id: String,
    val templateId: String,
    val title: String,
    val description: String,
    val category: MissionCategory,
    val difficulty: MissionDifficulty,
    val xpReward: Int,
    val coinReward: Int,
    val isCompleted: Boolean,
    val completedAtEpochSecond: Long,
    val dueEpochDay: Int,
    val createdAtEpochDay: Int,
    val isRecurring: Boolean,
    val isCustom: Boolean,
    val durationMinutes: Int?,
    val sortOrder: Int,
) {
    val status: MissionStatus
        get() = if (isCompleted) MissionStatus.COMPLETADA else MissionStatus.PENDIENTE

    /** A mission can only be undone while its day is still today. */
    fun canBeUncompleted(todayEpochDay: Int): Boolean =
        isCompleted && dueEpochDay == todayEpochDay
}

/** UI filtering state, kept as an immutable value. */
data class MissionFilter(
    val category: MissionCategory? = null,
    val status: MissionStatus? = null,
    val query: String = "",
) {
    val isActive: Boolean
        get() = category != null || status != null || query.isNotBlank()

    fun matches(mission: Mission): Boolean {
        if (category != null && mission.category != category) return false
        if (status != null && mission.status != status) return false
        if (query.isNotBlank()) {
            val needle = query.trim()
            val haystack = "${mission.title} ${mission.description}"
            if (!haystack.contains(needle, ignoreCase = true)) return false
        }
        return true
    }
}
