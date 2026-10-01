package com.mision.app.core.gamification

import com.mision.app.domain.model.StreakMilestone
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.StreakUpdate

/**
 * Deterministic, time-zone independent streak arithmetic.
 *
 * All inputs are *calendar days* (proleptic Julian days), never raw
 * timestamps, so a mission completed at 23:59 and one completed at 00:01
 * always belong to different days and reopening the app days later cannot
 * fabricate a streak.
 */
object StreakCalculator {

    /** Milestones that unlock a reward the first time they are reached. */
    val milestones: List<StreakMilestone> = listOf(
        StreakMilestone(3, 60, 25, "¡Racha de 3 días!", "La constancia empieza a notarse."),
        StreakMilestone(7, 150, 60, "¡Racha de 7 días!", "Una semana completa. ¡Imparable!"),
        StreakMilestone(14, 300, 120, "¡Racha de 14 días!", "Dos semanas seguidas de misiones."),
        StreakMilestone(30, 700, 250, "¡Racha de 30 días!", "Un mes entero cumpliendo tus misiones."),
        StreakMilestone(60, 1500, 500, "¡Racha de 60 días!", "Dos meses de disciplina real."),
        StreakMilestone(100, 3000, 1000, "¡Racha de 100 días!", "Cien días. Eres incontenible."),
        StreakMilestone(365, 15000, 5000, "¡Racha de 365 días!", "Un año completo. Leyenda."),
    )

    fun milestoneFor(days: Int): StreakMilestone? = milestones.firstOrNull { it.days == days }

    /**
     * Registers that the daily objective was completed on [day].
     * Completing twice on the same day is idempotent.
     */
    fun registerCompletedDay(state: StreakState, day: Int): StreakUpdate {
        if (state.lastCompletedEpochDay == day) {
            return StreakUpdate(state = state, isNewRecord = false, milestone = null, daysGained = 0)
        }

        val consecutive = state.lastCompletedEpochDay == day - 1
        val newCurrent = if (consecutive) state.currentStreak + 1 else 1
        val previousLongest = state.longestStreak
        val newLongest = maxOf(previousLongest, newCurrent)

        val updated = StreakState(
            currentStreak = newCurrent,
            longestStreak = newLongest,
            lastCompletedEpochDay = day,
            totalActiveDays = state.totalActiveDays + 1,
            isActive = true,
        )

        return StreakUpdate(
            state = updated,
            isNewRecord = newCurrent > previousLongest && newCurrent > 1,
            milestone = milestoneFor(newCurrent),
            daysGained = 1,
        )
    }

    /** True when [state] still points at a day that has not been skipped. */
    fun isActive(state: StreakState, today: Int): Boolean = when (state.lastCompletedEpochDay) {
        StreakState.NEVER -> false
        else -> today - state.lastCompletedEpochDay <= 1
    }

    /**
     * Effective view of a stored state: a streak broken by a missed day is
     * reported as zero without destroying the historical record.
     */
    fun effectiveState(state: StreakState, today: Int): StreakState {
        if (isActive(state, today)) return state.copy(isActive = true)
        return state.copy(currentStreak = 0, isActive = false)
    }

    /** First day of the streak, useful for calendars and debugging. */
    fun streakStartDay(state: StreakState): Int? =
        if (state.lastCompletedEpochDay == StreakState.NEVER || state.currentStreak <= 0) null
        else state.lastCompletedEpochDay - (state.currentStreak - 1)

    /**
     * Undoes [registerCompletedDay] for [day], used when the user reverts a
     * mission. Only the day that owns the tip of the streak is affected, so
     * undoing an older completion can never inflate or corrupt the chain.
     */
    fun revertCompletedDay(state: StreakState, day: Int): StreakState {
        if (state.lastCompletedEpochDay != day || state.totalActiveDays <= 0) return state
        val previous = state.currentStreak - 1
        val totalActiveDays = state.totalActiveDays - 1
        return if (previous <= 0) {
            StreakState(
                currentStreak = 0,
                longestStreak = state.longestStreak,
                lastCompletedEpochDay = StreakState.NEVER,
                totalActiveDays = totalActiveDays,
                isActive = false,
            )
        } else {
            state.copy(
                currentStreak = previous,
                lastCompletedEpochDay = day - 1,
                totalActiveDays = totalActiveDays,
                isActive = true,
            )
        }
    }
}
