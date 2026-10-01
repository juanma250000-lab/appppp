package com.mision.app.core.gamification

import com.mision.app.domain.model.StreakMilestone
import com.mision.app.domain.model.StreakState
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    private val day = 20_000

    private fun state(
        current: Int,
        longest: Int = current,
        lastDay: Int = day - 1,
        total: Int = current,
        active: Boolean = true,
    ) = StreakState(
        currentStreak = current,
        longestStreak = longest,
        lastCompletedEpochDay = lastDay,
        totalActiveDays = total,
        isActive = active,
    )

    // ---- Starting and continuing -------------------------------------------
    @Test
    fun `first completed day starts a streak of one`() {
        val update = StreakCalculator.registerCompletedDay(StreakState.empty(), day)

        assertEquals(1, update.state.currentStreak)
        assertEquals(1, update.state.longestStreak)
        assertEquals(day, update.state.lastCompletedEpochDay)
        assertEquals(1, update.state.totalActiveDays)
        assertTrue(update.state.isActive)
        assertFalse(update.isNewRecord)
        assertNull(update.milestone)
        assertEquals(1, update.daysGained)
    }

    @Test
    fun `completing twice on the same day is idempotent`() {
        val first = StreakCalculator.registerCompletedDay(StreakState.empty(), day)
        val second = StreakCalculator.registerCompletedDay(first.state, day)

        assertEquals(first.state, second.state)
        assertEquals(0, second.daysGained)
        assertFalse(second.isNewRecord)
    }

    @Test
    fun `consecutive days extend the chain`() {
        var current = StreakCalculator.registerCompletedDay(StreakState.empty(), day).state
        current = StreakCalculator.registerCompletedDay(current, day + 1).state

        assertEquals(2, current.currentStreak)
        assertEquals(2, current.totalActiveDays)
    }

    @Test
    fun `a missed day resets the chain but keeps the record`() {
        val broken = state(current = 4, lastDay = day - 5, total = 12)
        val update = StreakCalculator.registerCompletedDay(broken, day)

        assertEquals(1, update.state.currentStreak)
        assertEquals(4, update.state.longestStreak)
        assertEquals(12 + 1, update.state.totalActiveDays)
        assertFalse(update.isNewRecord)
    }

    @Test
    fun `beating the record is only reported when it really beats it`() {
        val onTheRecord = state(current = 5, longest = 5, lastDay = day - 1)
        assertTrue(StreakCalculator.registerCompletedDay(onTheRecord, day).isNewRecord)

        val belowRecord = state(current = 5, longest = 9, lastDay = day - 1)
        assertFalse(StreakCalculator.registerCompletedDay(belowRecord, day).isNewRecord)

        // The very first day is not a "record".
        val brand = StreakState.empty().copy(longestStreak = 0)
        assertFalse(StreakCalculator.registerCompletedDay(brand, day).isNewRecord)
    }

    // ---- Milestones ---------------------------------------------------------
    @Test
    fun `milestones exist for the documented days`() {
        assertEquals(listOf(3, 7, 14, 30, 60, 100, 365), StreakCalculator.milestones.map { it.days })
    }

    @Test
    fun `reaching a milestone reports it exactly once`() {
        var current = StreakState.empty()
        val fired = mutableListOf<StreakMilestone>()

        for (offset in 0..3) {
            val update = StreakCalculator.registerCompletedDay(current, day + offset)
            current = update.state
            update.milestone?.let { fired += it }
        }

        assertEquals(4, current.currentStreak)
        assertEquals(1, fired.size)
        assertEquals(3, fired.single().days)
        assertEquals(60, fired.single().rewardXp)
        assertEquals(25, fired.single().rewardCoins)
    }

    @Test
    fun `milestone lookup only answers for exact days`() {
        assertNotNull(StreakCalculator.milestoneFor(7))
        assertNull(StreakCalculator.milestoneFor(8))
        assertNull(StreakCalculator.milestoneFor(1))
    }

    // ---- Time boundaries ----------------------------------------------------
    @Test
    fun `missions finished minutes apart across midnight belong to different days`() {
        val beforeMidnight = LocalDateTime.of(2026, 9, 15, 23, 59)
        val afterMidnight = LocalDateTime.of(2026, 9, 16, 0, 1)

        val firstDay = beforeMidnight.toLocalDate().toEpochDay().toInt()
        val secondDay = afterMidnight.toLocalDate().toEpochDay().toInt()

        assertEquals(1, secondDay - firstDay)

        val first = StreakCalculator.registerCompletedDay(StreakState.empty(), firstDay)
        val second = StreakCalculator.registerCompletedDay(first.state, secondDay)

        assertEquals(2, second.state.currentStreak)
        assertEquals(secondDay, second.state.lastCompletedEpochDay)
    }

    @Test
    fun `reopening days later does not fabricate a streak`() {
        val lastWeek = state(current = 5, lastDay = day - 7, total = 5)
        val today = StreakCalculator.effectiveState(lastWeek, day)

        assertEquals(0, today.currentStreak)
        assertFalse(today.isActive)
        // The historical record survives the reset.
        assertEquals(5, today.longestStreak)
    }

    @Test
    fun `a streak is still active on the day after the last completion`() {
        val yesterday = state(current = 3, lastDay = day - 1)

        assertTrue(StreakCalculator.isActive(yesterday, day))
        assertTrue(StreakCalculator.effectiveState(yesterday, day).isActive)
        assertFalse(StreakCalculator.isActive(yesterday, day + 2))
        assertFalse(StreakCalculator.isActive(StreakState.empty(), day))
    }

    @Test
    fun `streak start day is derived from the chain length`() {
        assertEquals(day - 2, StreakCalculator.streakStartDay(state(current = 3, lastDay = day)))
        assertNull(StreakCalculator.streakStartDay(StreakState.empty()))
    }

    // ---- Undo ---------------------------------------------------------------
    @Test
    fun `undoing the tip of the chain steps it back`() {
        val streak = state(current = 3, lastDay = day, total = 10)
        val reverted = StreakCalculator.revertCompletedDay(streak, day)

        assertEquals(2, reverted.currentStreak)
        assertEquals(day - 1, reverted.lastCompletedEpochDay)
        assertEquals(9, reverted.totalActiveDays)
        assertTrue(reverted.isActive)
        assertEquals(3, reverted.longestStreak)
    }

    @Test
    fun `undoing the only completed day clears the chain`() {
        val streak = StreakCalculator.registerCompletedDay(StreakState.empty(), day).state
        val reverted = StreakCalculator.revertCompletedDay(streak, day)

        assertEquals(0, reverted.currentStreak)
        assertEquals(StreakState.NEVER, reverted.lastCompletedEpochDay)
        assertEquals(0, reverted.totalActiveDays)
        assertFalse(reverted.isActive)
        assertEquals(1, reverted.longestStreak)
    }

    @Test
    fun `undoing an older day never corrupts the chain`() {
        val streak = state(current = 5, lastDay = day, total = 20)
        val untouched = StreakCalculator.revertCompletedDay(streak, day - 3)

        assertEquals(streak, untouched)
        assertEquals(
            StreakState.empty(),
            StreakCalculator.revertCompletedDay(StreakState.empty(), day),
        )
    }
}
