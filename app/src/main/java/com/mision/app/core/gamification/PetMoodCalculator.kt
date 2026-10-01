package com.mision.app.core.gamification

import com.mision.app.domain.model.PetMood

/**
 * Rules that translate user activity into the pet's emotional state.
 * Pure and unit tested: the same inputs always produce the same mood.
 */
object PetMoodCalculator {

    const val INACTIVE_DAYS_TIRED = 3
    const val INACTIVE_DAYS_SAD = 6

    /**
     * @param completionRatio 0f..1f daily completion
     * @param energy pet energy 0..100
     * @param happiness pet happiness 0..100
     * @param daysSinceInteraction whole days since the last interaction
     * @param allCompleted true when today's missions are all done
     * @param newStreakRecord true when the streak just beat its record
     * @param momentaryBoost forces a celebration right after a reward
     */
    fun calculate(
        completionRatio: Float,
        energy: Int,
        happiness: Int,
        daysSinceInteraction: Int,
        allCompleted: Boolean,
        newStreakRecord: Boolean,
        momentaryBoost: Boolean = false,
    ): PetMood {
        if (momentaryBoost || allCompleted) return PetMood.CELEBRANDO
        if (newStreakRecord) return PetMood.ORGULLOSO
        if (daysSinceInteraction >= INACTIVE_DAYS_SAD) return PetMood.TRISTE
        if (daysSinceInteraction >= INACTIVE_DAYS_TIRED || energy <= 25) return PetMood.CANSADO
        if (happiness <= 30) return PetMood.TRISTE
        if (completionRatio >= 0.5f || happiness >= 75) return PetMood.FELIZ
        if (completionRatio > 0f) return PetMood.MOTIVADO
        return PetMood.MOTIVADO
    }
}
