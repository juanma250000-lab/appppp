package com.mision.app.core.gamification

import com.mision.app.domain.model.PetMood
import org.junit.Assert.assertEquals
import org.junit.Test

class PetMoodCalculatorTest {

    private fun mood(
        ratio: Float = 0f,
        energy: Int = 80,
        happiness: Int = 70,
        daysSinceInteraction: Int = 0,
        allCompleted: Boolean = false,
        newStreakRecord: Boolean = false,
        momentaryBoost: Boolean = false,
    ) = PetMoodCalculator.calculate(
        completionRatio = ratio,
        energy = energy,
        happiness = happiness,
        daysSinceInteraction = daysSinceInteraction,
        allCompleted = allCompleted,
        newStreakRecord = newStreakRecord,
        momentaryBoost = momentaryBoost,
    )

    @Test
    fun `finishing everything always celebrates`() {
        assertEquals(PetMood.CELEBRANDO, mood(allCompleted = true))
        // Even with a exhausted pet, the achievement wins.
        assertEquals(PetMood.CELEBRANDO, mood(allCompleted = true, energy = 0, happiness = 0))
        assertEquals(PetMood.CELEBRANDO, mood(momentaryBoost = true))
    }

    @Test
    fun `a new record makes the pet proud`() {
        assertEquals(PetMood.ORGULLOSO, mood(newStreakRecord = true))
    }

    @Test
    fun `inactivity turns the pet sad after the documented days`() {
        assertEquals(PetMood.CANSADO, mood(daysSinceInteraction = PetMoodCalculator.INACTIVE_DAYS_TIRED))
        assertEquals(
            PetMood.TRISTE,
            mood(daysSinceInteraction = PetMoodCalculator.INACTIVE_DAYS_SAD),
        )
        assertEquals(
            PetMood.MOTIVADO,
            mood(daysSinceInteraction = PetMoodCalculator.INACTIVE_DAYS_TIRED - 1),
        )
    }

    @Test
    fun `low energy makes the pet tired`() {
        assertEquals(PetMood.CANSADO, mood(energy = 25))
        assertEquals(PetMood.FELIZ, mood(energy = 26, happiness = 80))
    }

    @Test
    fun `low happiness makes the pet sad`() {
        assertEquals(PetMood.TRISTE, mood(happiness = 30))
        assertEquals(PetMood.FELIZ, mood(happiness = 31, ratio = 0.6f))
    }

    @Test
    fun `progress and high happiness lift the mood`() {
        assertEquals(PetMood.FELIZ, mood(ratio = 0.5f))
        assertEquals(PetMood.FELIZ, mood(ratio = 0.0f, happiness = 75))
        assertEquals(PetMood.MOTIVADO, mood(ratio = 0.25f, happiness = 60))
        assertEquals(PetMood.MOTIVADO, mood(ratio = 0f, happiness = 60))
    }

    @Test
    fun `celebration beats sadness and sadness beats everything else`() {
        // Priority: celebration > record > inactivity > energy/happiness.
        assertEquals(
            PetMood.CELEBRANDO,
            mood(allCompleted = true, daysSinceInteraction = 10, energy = 0, happiness = 0),
        )
        assertEquals(
            PetMood.ORGULLOSO,
            mood(newStreakRecord = true, daysSinceInteraction = 10, energy = 0),
        )
        assertEquals(
            PetMood.TRISTE,
            mood(daysSinceInteraction = 10, energy = 0),
        )
    }
}
