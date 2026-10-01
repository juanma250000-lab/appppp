package com.mision.app.core.gamification

import com.mision.app.domain.model.AchievementStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEvaluatorTest {

    private fun stats(
        missions: Int = 0,
        streak: Int = 0,
        perfectDays: Int = 0,
        level: Int = 1,
        coinsEarned: Int = 0,
        purchases: Int = 0,
    ) = AchievementStats(
        totalMissionsCompleted = missions,
        longestStreak = streak,
        perfectDays = perfectDays,
        level = level,
        coinsEarned = coinsEarned,
        purchases = purchases,
    )

    @Test
    fun `a brand new profile unlocks nothing`() {
        assertTrue(AchievementEvaluator.newlyUnlocked(stats(), alreadyUnlocked = emptySet()).isEmpty())
    }

    @Test
    fun `the first mission unlocks the first step`() {
        val unlocked = AchievementEvaluator.newlyUnlocked(stats(missions = 1), emptySet())

        assertEquals(listOf("primer_paso"), unlocked.map { it.id })
        assertEquals(50, unlocked.single().rewardXp)
        assertEquals(10, unlocked.single().rewardCoins)
    }

    @Test
    fun `achievement thresholds unlock exactly at the target`() {
        val belowTen = AchievementEvaluator.newlyUnlocked(stats(missions = 9), emptySet())
        assertEquals(listOf("primer_paso"), belowTen.map { it.id })

        val atTen = AchievementEvaluator.newlyUnlocked(stats(missions = 10), emptySet()).map { it.id }
        assertTrue("en_marcha" in atTen)

        val atHundred = AchievementEvaluator.newlyUnlocked(stats(missions = 100), emptySet()).map { it.id }
        assertTrue("maestro_de_habitos" in atHundred)
        assertTrue("en_marcha" in atHundred)
    }

    @Test
    fun `streak, perfect day, level, coins and shop each have their own rule`() {
        val streakUnlocked = AchievementEvaluator.newlyUnlocked(stats(streak = 7), emptySet())
        assertEquals(listOf("constancia"), streakUnlocked.map { it.id })

        val perfectDay = AchievementEvaluator.newlyUnlocked(stats(perfectDays = 1), emptySet())
        assertEquals(listOf("completista"), perfectDay.map { it.id })

        val levelFive = AchievementEvaluator.newlyUnlocked(stats(level = 5), emptySet())
        assertEquals(listOf("ascenso"), levelFive.map { it.id })

        val rich = AchievementEvaluator.newlyUnlocked(stats(coinsEarned = 1000), emptySet())
        assertEquals(listOf("cofre_lleno"), rich.map { it.id })

        val shopper = AchievementEvaluator.newlyUnlocked(stats(purchases = 5), emptySet())
        assertEquals(listOf("coleccionista"), shopper.map { it.id })
    }

    @Test
    fun `already unlocked achievements never fire again`() {
        val unlocked = AchievementEvaluator.newlyUnlocked(
            stats = stats(missions = 1, streak = 7),
            alreadyUnlocked = setOf("primer_paso"),
        )

        assertEquals(listOf("constancia"), unlocked.map { it.id })
    }

    @Test
    fun `progress is clamped between zero and one`() {
        val firstStep = AchievementCatalog.byId("primer_paso")!!
        assertEquals(0f, firstStep.progressOf(stats(missions = 0)), 0.0001f)
        assertEquals(1f, firstStep.progressOf(stats(missions = 1)), 0.0001f)
        assertEquals(1f, firstStep.progressOf(stats(missions = 50)), 0.0001f)

        val tenMissions = AchievementCatalog.byId("en_marcha")!!
        assertEquals(0.5f, tenMissions.progressOf(stats(missions = 5)), 0.0001f)
        assertEquals(5, tenMissions.currentValue(stats(missions = 5)))
    }

    @Test
    fun `the catalogue has unique ids and spanish copy`() {
        val ids = AchievementCatalog.all.map { it.id }
        assertEquals(ids.distinct(), ids)
        AchievementCatalog.all.forEach { definition ->
            assertTrue("nombre vacío en ${definition.id}", definition.name.isNotBlank())
            assertTrue("descripción vacía en ${definition.id}", definition.description.isNotBlank())
            assertTrue("objetivo inválido en ${definition.id}", definition.target > 0)
        }
    }
}
