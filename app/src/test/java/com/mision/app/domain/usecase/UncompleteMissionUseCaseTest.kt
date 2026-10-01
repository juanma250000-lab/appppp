package com.mision.app.domain.usecase

import com.mision.app.testing.DirectTransactionRunner
import com.mision.app.testing.FakeClock
import com.mision.app.testing.FakeGamificationRepository
import com.mision.app.testing.FakeMissionRepository
import com.mision.app.testing.FakePetRepository
import com.mision.app.testing.FakeProgressRepository
import com.mision.app.domain.model.StreakState
import com.mision.app.testing.testMission
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UncompleteMissionUseCaseTest {

    private val clock = FakeClock()
    private val missions = FakeMissionRepository()
    private val progress = FakeProgressRepository()
    private val gamification = FakeGamificationRepository(progress)
    private val pet = FakePetRepository()

    private lateinit var complete: CompleteMissionUseCase
    private lateinit var uncomplete: UncompleteMissionUseCase

    private val today get() = clock.epochDay

    @Before
    fun setUp() {
        complete = CompleteMissionUseCase(
            missionRepository = missions,
            progressRepository = progress,
            gamificationRepository = gamification,
            shopRepository = com.mision.app.testing.FakeShopRepository(),
            petRepository = pet,
            transaction = DirectTransactionRunner,
            clock = clock,
        )
        uncomplete = UncompleteMissionUseCase(
            missionRepository = missions,
            progressRepository = progress,
            gamificationRepository = gamification,
            petRepository = pet,
            transaction = DirectTransactionRunner,
            clock = clock,
        )
    }

    @Test
    fun `undoing a completion removes the rewards of that mission only`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a", xpReward = 25, coinReward = 10))
        // A second, pending mission keeps the day open (no perfect-day bonus).
        missions.add(testMission(today, id = "z:$today", templateId = "z"))

        val gained = complete("a:$today")!!
        // 25 from the mission + 50 from the "Primer paso" achievement bonus.
        assertEquals(75, progress.getProfile().totalXp)
        assertEquals(20, progress.getProfile().coins)

        val undone = uncomplete("a:$today")

        assertTrue(undone)
        assertFalse(missions.getMission("a:$today")!!.isCompleted)
        // Achievement rewards already granted stay, only the mission's do not.
        assertEquals(75 - gained.mission.xpReward, progress.getProfile().totalXp)
        assertEquals(20 - gained.mission.coinReward, progress.getProfile().coins)
        assertEquals(0, progress.getProfile().totalMissionsCompleted)
    }

    @Test
    fun `undoing a completion steps the streak back`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a"))

        complete("a:$today")
        assertEquals(1, gamification.getStreak().currentStreak)

        uncomplete("a:$today")
        assertEquals(0, gamification.getStreak().currentStreak)
        assertEquals(StreakState.NEVER, gamification.getStreak().lastCompletedEpochDay)
        assertEquals(0, gamification.getStreak().totalActiveDays)
    }

    @Test
    fun `the daily log of the day is corrected`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a", xpReward = 25, coinReward = 10))
        // A second, pending mission keeps the day open (no perfect-day bonus).
        missions.add(testMission(today, id = "z:$today", templateId = "z"))

        complete("a:$today")
        uncomplete("a:$today")

        val log = progress.getDailyLog(today)
        assertEquals(0, log!!.completedCount)
        assertEquals(2, log.totalMissions)
        // The mission rewards are removed; the achievement bonus already granted stays.
        assertEquals(50, log.xpEarned)
        assertEquals(10, log.coinsEarned)
        assertFalse(log.isPerfectDay)
    }

    @Test
    fun `missions of previous days cannot be undone`() = runTest {
        missions.add(
            testMission(today - 1, id = "old:${today - 1}", templateId = "old", isCompleted = true),
        )

        assertFalse(uncomplete("old:${today - 1}"))
        assertTrue(missions.getMission("old:${today - 1}")!!.isCompleted)
    }

    @Test
    fun `pending missions cannot be undone`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a"))

        assertFalse(uncomplete("a:$today"))
        assertEquals(0, progress.getProfile().totalMissionsCompleted)
    }

    @Test
    fun `undoing twice only reverts once`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a"))

        complete("a:$today")
        assertTrue(uncomplete("a:$today"))
        val afterFirst = progress.getProfile()

        assertFalse(uncomplete("a:$today"))
        assertEquals(afterFirst, progress.getProfile())
        assertEquals(0, progress.getProfile().totalMissionsCompleted)
        assertEquals(0, progress.getDailyLog(today)!!.completedCount)
    }

    @Test
    fun `counters never drop below zero`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a", xpReward = 25, coinReward = 10))
        // A second, pending mission keeps the day open (no perfect-day bonus).
        missions.add(testMission(today, id = "z:$today", templateId = "z"))

        complete("a:$today")
        progress.saveProfile(progress.getProfile().copy(totalXp = 5, coins = 3))
        uncomplete("a:$today")

        assertEquals(0, progress.getProfile().totalXp)
        assertEquals(0, progress.getProfile().coins)
        // The daily log only loses the mission's own rewards.
        assertEquals(50, progress.getDailyLog(today)!!.xpEarned)
        assertEquals(10, progress.getDailyLog(today)!!.coinsEarned)
    }

    @Test
    fun `undoing one of several completions keeps the streak of the day`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a"))
        missions.add(testMission(today, id = "b:$today", templateId = "b"))

        complete("a:$today")
        complete("b:$today")
        uncomplete("b:$today")

        // "a" is still done, so today still counts for the streak.
        assertEquals(1, gamification.getStreak().currentStreak)
        assertEquals(today, gamification.getStreak().lastCompletedEpochDay)

        uncomplete("a:$today")
        assertEquals(0, gamification.getStreak().currentStreak)
    }

    @Test
    fun `undoing and redoing a milestone day never pays the milestone twice`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a"))
        // Two days in a row already: completing today reaches the 3 day milestone.
        gamification.saveStreak(
            StreakState(
                currentStreak = 2,
                longestStreak = 2,
                lastCompletedEpochDay = today - 1,
                totalActiveDays = 2,
                isActive = true,
            ),
        )

        val first = complete("a:$today")!!
        assertEquals(3, first.milestone?.days)
        val afterFirst = progress.getProfile()

        assertTrue(uncomplete("a:$today"))
        assertEquals(2, gamification.getStreak().currentStreak)

        val second = complete("a:$today")!!
        assertEquals(3, second.milestone?.days)
        // Same balance as after the first completion: the milestone was taken
        // back on undo and paid only once in total.
        assertEquals(afterFirst.coins, progress.getProfile().coins)
        assertEquals(afterFirst.totalXp, progress.getProfile().totalXp)
    }
}
