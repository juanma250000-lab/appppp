package com.mision.app.core.gamification

import com.mision.app.domain.model.Pet

/**
 * Every line the pet can say. Keeping the copy here makes the speech bubble
 * consistent across screens and easy to review for tone.
 */
object PetSpeechProvider {

    /** Home bubble: reacts to today's completion ratio (0f..1f). */
    fun forDailyProgress(ratio: Float): String = when {
        ratio <= 0f -> "Vamos, todavía tenemos mucho por hacer."
        ratio < 0.5f -> "¡Buen comienzo! Ya llevas el ${(ratio * 100).toInt()} % del día."
        ratio < 0.75f -> "¡Muy bien! Ya llevamos la mitad."
        ratio < 1f -> "¡Casi lo conseguimos! Queda poco para cerrar el día."
        else -> "¡Día completado! ¡Lo conseguimos!"
    }

    /** Pet screen bubble: reacts to the pet's own state. */
    fun forPetState(pet: Pet, todayEpochDay: Int): String {
        val inactiveDays = (todayEpochDay - pet.lastInteractionEpochDay).coerceAtLeast(0)
        return when {
            inactiveDays >= PetMoodCalculator.INACTIVE_DAYS_SAD -> "¡Cuánto tiempo! Te he echado de menos."
            pet.energy <= LOW_ENERGY -> "Necesito descansar un poco. ¿Me ayudas?"
            pet.happiness >= HIGH_HAPPINESS -> "¡Qué buen día! Hacemos un gran equipo."
            else -> motivationalLine(pet.happiness + todayEpochDay)
        }
    }

    /** Rotating motivational line; the same [seed] always returns the same line. */
    fun motivationalLine(seed: Int): String =
        motivationalMessages[Math.floorMod(seed, motivationalMessages.size)]

    private const val LOW_ENERGY = 25
    private const val HIGH_HAPPINESS = 75

    private val motivationalMessages: List<String> = listOf(
        "Cada misión cumplida te acerca un poco más a la persona que quieres ser.",
        "No hace falta ser perfecto: constante sí.",
        "Tu yo de mañana te va a agradecer lo que hagas hoy.",
        "Pequeños pasos, grandes rachas.",
        "La motivación va y viene; tus hábitos se quedan.",
        "Hoy también cuentan las misiones pequeñas.",
        "Progresar se siente mejor que posponer.",
        "Tu mascota cree en ti, y con razón.",
    )
}
