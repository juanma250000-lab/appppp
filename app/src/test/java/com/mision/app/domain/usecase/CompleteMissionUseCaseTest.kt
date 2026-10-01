package com.mision.app.domain.usecase

import com.mision.app.core.gamification.AchievementCatalog
import com.mision.app.domain.model.PetMood
import com.mision.app.testing.FakeClock
import com.mision.app.testing.FakeGamificationRepository
import com.mision.app.testing.FakeMissionRepository
import com.mision.app.testing.FakePetRepository
import com.mision.app.testing.FakeProgressRepository
import com.mision.app.testing.FakeShopRepository
import com.mision.app.testing.testMission
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompleteMissionUseCaseTest {

    private val clock = FakeClock()
    private val missions = FakeMissionRepository()
    private val progress = FakeProgressRepository()
    private val gamification = FakeGamificationRepository(progress)
    private val shop = FakeShopRepository()
    private val pet = FakePetRepository()
    private lateinit var useCase: CompleteMissionUseCase

    private val today get() = clock.epochDay

    @Before
    fun setUp() {
        useCase = CompleteMissionUseCase(
            missionRepository = missions,
            progressRepository = progress,
            gamificationRepository = gamification,
            shopRepository = shop,
            petRepository = pet,
            clock = clock,
        )
    }

    private fun seedTwoMissions() {
        missions.add(testMission(today, id = "a:$today", templateId = "a", xpReward = 25, coinReward = 10, sortOrder = 0))
        missions.add(testMission(today, id = "b:$today", templateId = "b", xpReward = 50, coinReward = 20, sortOrder = 1))
    }

    @Test
    fun `completing a mission pays xp and coins and updates every aggregate`() = runTest {
        seedTwoMissions()

        val result = useCase("a:$today")

        assertNotNull(result)
        assertTrue(result!!.mission.isCompleted)

        // 25 from the mission plus 50 from the "Primer paso" achievement.
        assertEquals(75, result.xpGained)
        assertEquals(50, result.bonusXp)
        assertEquals(20, result.coinsGained)
        assertEquals(listOf("primer_paso"), result.newAchievements.map { it.id })

        val profile = progress.getProfile()
        assertEquals(75, profile.totalXp)
        assertEquals(20, profile.coins)
        assertEquals(1, profile.totalMissionsCompleted)

        assertEquals(1, result.completedToday)
        assertEquals(2, result.totalToday)
        assertEquals(1, result.remainingToday)
        assertFalse(result.perfectDay)
        assertTrue(result.hasCelebration)
        assertFalse(result.leveledUp)
    }

    @Test
    fun `the daily log accumulates xp and coins of the day`() = runTest {
        seedTwoMissions()
        useCase("a:$today")
        useCase("b:$today")

        val log = progress.getDailyLog(today)
        assertNotNull(log)
        assertEquals(75 + 50, log!!.xpEarned)
        assertEquals(20 + 20, log.coinsEarned)
        assertEquals(2, log.completedCount)
        assertEquals(2, log.totalMissions)
        assertTrue(log.isPerfectDay)
    }

    @Test
    fun `completing every mission of the day is a perfect day and celebrates`() = runTest {
        seedTwoMissions()

        val first = useCase("a:$today")!!
        val second = useCase("b:$today")!!

        assertFalse(first.perfectDay)
        assertTrue(second.perfectDay)
        assertTrue(second.hasCelebration)
        assertEquals(2, second.completedToday)
        assertEquals(0, second.remainingToday)
        assertEquals(PetMood.CELEBRANDO, pet.getPet().mood)
    }

    @Test
    fun `the streak starts with the first completed day`() = runTest {
        seedTwoMissions()

        val result = useCase("a:$today")!!
        val streak = gamification.getStreak()

        assertEquals(1, streak.currentStreak)
        assertEquals(today, streak.lastCompletedEpochDay)
        assertTrue(streak.isActive)
        assertEquals(1, result.streakUpdate.daysGained)
        assertEquals(1, result.streakUpdate.state.currentStreak)
    }

    @Test
    fun `a level up is reported with both levels`() = runTest {
        seedTwoMissions()
        gamification.unlockAchievements(AchievementCatalog.all.map { it.id }, 0L)
        progress.saveProfile(progress.getProfile().copy(totalXp = 90))

        val result = useCase("a:$today")!!

        assertEquals(1, result.previousLevel)
        assertEquals(2, result.newLevel)
        assertTrue(result.leveledUp)
        assertTrue(result.hasCelebration)
        assertEquals(115, progress.getProfile().totalXp)
        assertEquals(2, progress.getProfile().level)
    }

    @Test
    fun `a mission from a previous day cannot be completed`() = runTest {
        missions.add(testMission(today - 1, id = "old:${today - 1}", templateId = "old"))

        val result = useCase("old:${today - 1}")

        assertNull(result)
        assertFalse(missions.getMission("old:${today - 1}")!!.isCompleted)
        assertEquals(0, progress.getProfile().totalMissionsCompleted)
        assertEquals(0, progress.getProfile().totalXp)
        assertEquals(0, gamification.getStreak().currentStreak)
    }

    @Test
    fun `a mission cannot be completed twice`() = runTest {
        missions.add(testMission(today, id = "a:$today", templateId = "a"))

        val first = useCase("a:$today")
        val second = useCase("a:$today")

        assertNotNull(first)
        assertNull(second)
        assertEquals(first!!.xpGained, progress.getProfile().totalXp)
        assertEquals(1, progress.getProfile().totalMissionsCompleted)
        assertEquals(1, first.completedToday)
    }

    @Test
    fun `an unknown mission returns null`() = runTest {
        assertNull(useCase("no-existe"))
    }

    @Test
    fun `the pet gains happiness and the mission xp`() = runTest {
        seedTwoMissions()
        val before = pet.getPet()

        useCase("a:$today")

        val after = pet.getPet()
        // The pet is fed with the mission's own XP; achievement bonuses belong
        // to the profile.
        assertEquals(before.xp + 25, after.xp)
        assertEquals(before.happiness + 4, after.happiness)
        assertEquals(today, after.lastInteractionEpochDay)
        assertTrue(after.happiness <= 100)
    }
}
