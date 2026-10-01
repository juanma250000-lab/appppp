package com.mision.app.core.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCalculatorTest {

    @Test
    fun `level 1 starts at zero xp`() {
        assertEquals(0, LevelCalculator.xpRequiredForLevel(1))
        assertEquals(1, LevelCalculator.levelFor(0))
        assertEquals(1, LevelCalculator.levelFor(-50))
    }

    @Test
    fun `cumulative curve matches the documented thresholds`() {
        assertEquals(100, LevelCalculator.xpRequiredForLevel(2))
        assertEquals(250, LevelCalculator.xpRequiredForLevel(3))
        assertEquals(450, LevelCalculator.xpRequiredForLevel(4))
        assertEquals(700, LevelCalculator.xpRequiredForLevel(5))
    }

    @Test
    fun `level is the highest level already reached`() {
        assertEquals(1, LevelCalculator.levelFor(99))
        assertEquals(2, LevelCalculator.levelFor(100))
        assertEquals(2, LevelCalculator.levelFor(249))
        assertEquals(3, LevelCalculator.levelFor(250))
        assertEquals(4, LevelCalculator.levelFor(450))
    }

    @Test
    fun `every threshold maps back to its own level`() {
        for (level in 1..LevelCalculator.MAX_LEVEL) {
            val xp = LevelCalculator.xpRequiredForLevel(level)
            assertEquals(
                "xp=$xp should be level $level",
                level,
                LevelCalculator.levelFor(xp),
            )
        }
    }

    @Test
    fun `xp needed to advance grows by 50 each level`() {
        assertEquals(100, LevelCalculator.xpToNext(1))
        assertEquals(150, LevelCalculator.xpToNext(2))
        assertEquals(200, LevelCalculator.xpToNext(3))
    }

    @Test
    fun `max level needs no further xp`() {
        assertEquals(0, LevelCalculator.xpToNext(LevelCalculator.MAX_LEVEL))
        val progress = LevelCalculator.progressFor(10_000_000)
        assertTrue(progress.isMaxLevel)
        assertEquals(1f, progress.progress, 0.0001f)
        assertEquals(LevelCalculator.MAX_LEVEL, progress.level)
    }

    @Test
    fun `progress reports the xp inside the current level`() {
        val atStart = LevelCalculator.progressFor(0)
        assertEquals(1, atStart.level)
        assertEquals(0, atStart.xpIntoLevel)
        assertEquals(100, atStart.xpToNext)
        assertEquals(0f, atStart.progress, 0.0001f)

        val halfway = LevelCalculator.progressFor(50)
        assertEquals(50, halfway.xpIntoLevel)
        assertEquals(50, halfway.xpToNext)
        assertEquals(0.5f, halfway.progress, 0.0001f)

        val atLevelTwo = LevelCalculator.progressFor(100)
        assertEquals(2, atLevelTwo.level)
        assertEquals(0, atLevelTwo.xpIntoLevel)
        assertEquals(150, atLevelTwo.xpToNext)
        assertEquals(0f, atLevelTwo.progress, 0.0001f)
    }

    @Test
    fun `progress never exceeds one hundred percent`() {
        val justBelow = LevelCalculator.progressFor(249)
        assertTrue(justBelow.progress < 1f)
        assertEquals(LevelCalculator.MAX_LEVEL, LevelCalculator.levelFor(Int.MAX_VALUE))
    }
}
