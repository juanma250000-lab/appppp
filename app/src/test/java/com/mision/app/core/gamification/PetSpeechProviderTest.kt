package com.mision.app.core.gamification

import com.mision.app.domain.model.Pet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetSpeechProviderTest {

    private val today = 20_000

    @Test
    fun `daily progress lines follow the completion ratio`() {
        assertEquals("Vamos, todavía tenemos mucho por hacer.", PetSpeechProvider.forDailyProgress(0f))
        assertTrue(PetSpeechProvider.forDailyProgress(0.25f).contains("25 %"))
        assertEquals("¡Día completado! ¡Lo conseguimos!", PetSpeechProvider.forDailyProgress(1f))
    }

    @Test
    fun `a long absence is acknowledged before anything else`() {
        val pet = Pet.default(epochDay = today - PetMoodCalculator.INACTIVE_DAYS_SAD).copy(energy = 10)

        assertEquals("¡Cuánto tiempo! Te he echado de menos.", PetSpeechProvider.forPetState(pet, today))
    }

    @Test
    fun `motivational lines are deterministic for any seed`() {
        assertEquals(PetSpeechProvider.motivationalLine(-3), PetSpeechProvider.motivationalLine(-3))
        assertTrue(PetSpeechProvider.motivationalLine(Int.MIN_VALUE).isNotBlank())
    }
}
