package com.mision.app.core.gamification

import kotlin.math.sqrt

/**
 * Progression curve.
 *
 * Level 1 starts at 0 XP and every level up costs 50 XP more than the previous
 * one (100, 150, 200, ...), which produces the cumulative curve
 * 0, 100, 250, 450, 700 ... The formula is closed form so the UI never needs
 * to iterate and balancing only requires touching [BASE_COST] / [GROWTH].
 */
object LevelCalculator {

    /** XP needed to move from level n to n+1 = BASE_COST + (n - 1) * GROWTH. */
    const val BASE_COST = 100
    const val GROWTH = 50
    const val MAX_LEVEL = 60

    /** Cumulative XP required to *reach* [level]. */
    fun xpRequiredForLevel(level: Int): Int {
        val safeLevel = level.coerceIn(1, MAX_LEVEL)
        // Sum of an arithmetic series: 25 * (n - 1) * (n + 2)
        return 25 * (safeLevel - 1) * (safeLevel + 2)
    }

    /** Level attained with the given total XP. */
    fun levelFor(totalXp: Int): Int {
        if (totalXp <= 0) return 1
        // Solve 25 * (n - 1) * (n + 2) <= xp  =>  n = (-1 + sqrt(1 + 4 * (2 + xp / 25))) / 2
        val solved = (-1 + sqrt(1.0 + 4.0 * (2.0 + totalXp / 25.0))) / 2.0
        return solved.toInt().coerceIn(1, MAX_LEVEL)
    }

    /** XP needed to advance from [level] to the next one. */
    fun xpToNext(level: Int): Int {
        if (level >= MAX_LEVEL) return 0
        return BASE_COST + (level - 1) * GROWTH
    }

    fun progressFor(totalXp: Int): LevelProgress {
        val level = levelFor(totalXp)
        if (level >= MAX_LEVEL) {
            return LevelProgress(
                level = level,
                xpIntoLevel = 0,
                xpToNext = 0,
                progress = 1f,
                isMaxLevel = true,
                totalXp = totalXp,
            )
        }
        val floor = xpRequiredForLevel(level)
        val ceiling = xpRequiredForLevel(level + 1)
        val span = (ceiling - floor).coerceAtLeast(1)
        val into = (totalXp - floor).coerceAtLeast(0)
        return LevelProgress(
            level = level,
            xpIntoLevel = into,
            xpToNext = span - into.coerceAtMost(span),
            progress = (into.toFloat() / span).coerceIn(0f, 1f),
            isMaxLevel = false,
            totalXp = totalXp,
        )
    }
}

/** Immutable description of a level-up state. */
data class LevelProgress(
    val level: Int,
    val xpIntoLevel: Int,
    val xpToNext: Int,
    val progress: Float,
    val isMaxLevel: Boolean,
    val totalXp: Int,
)
