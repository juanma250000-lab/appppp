package com.mision.app.core.gamification

import com.mision.app.domain.model.PetMessage
import com.mision.app.domain.model.PetSpeechState

/**
 * All contextual lines the pet can say. Keeping the copy here makes the
 * speech bubble reusable across screens and easy to review for tone.
 */
object PetSpeechProvider {

    /** Bucket for a 0f..1f daily completion ratio. */
    fun stateForProgress(ratio: Float): PetSpeechState = when {
        ratio <= 0f -> PetSpeechState.ZERO_PROGRESS
        ratio < 0.5f -> PetSpeechState.EARLY_PROGRESS
        ratio < 0.75f -> PetSpeechState.HALF_PROGRESS
        ratio < 1f -> PetSpeechState.NEAR_COMPLETE
        else -> PetSpeechState.DAY_COMPLETE
    }

    fun message(
        state: PetSpeechState,
        progressPercent: Int = 0,
        petName: String = "",
        streakDays: Int = 0,
        xpGained: Int = 0,
    ): PetMessage = when (state) {
        PetSpeechState.ZERO_PROGRESS -> PetMessage(
            state,
            "Vamos, todavía tenemos mucho por hacer.",
        )

        PetSpeechState.EARLY_PROGRESS -> PetMessage(
            state,
            "¡Buen comienzo! Ya llevas $progressPercent % del día.",
        )

        PetSpeechState.HALF_PROGRESS -> PetMessage(
            state,
            "¡Muy bien! Ya llevamos la mitad.",
        )

        PetSpeechState.NEAR_COMPLETE -> PetMessage(
            state,
            "¡Casi lo conseguimos! Queda poco para cerrar el día.",
        )

        PetSpeechState.DAY_COMPLETE -> PetMessage(
            state,
            "¡Día completado! ¡Lo conseguimos!",
            priority = 10,
        )

        PetSpeechState.MORNING -> PetMessage(
            state,
            "¡Buenos días! Hoy toca avanzar, paso a paso.",
        )

        PetSpeechState.AFTERNOON -> PetMessage(
            state,
            "Buenas tardes. Vamos por muy buen camino.",
        )

        PetSpeechState.EVENING -> PetMessage(
            state,
            "Buenas noches. Cerramos el día con broche dorado.",
        )

        PetSpeechState.STREAK_MILESTONE -> PetMessage(
            state,
            "¡$streakDays días seguidos! La constancia te define.",
            priority = 9,
        )

        PetSpeechState.LEVEL_UP -> PetMessage(
            state,
            "¡Nuevo nivel! Sigues creciendo conmigo.",
            priority = 10,
        )

        PetSpeechState.ACHIEVEMENT -> PetMessage(
            state,
            "¡Recompensa desbloqueada! Merecida.",
            priority = 8,
        )

        PetSpeechState.FIRST_OPEN -> PetMessage(
            state,
            if (petName.isBlank()) "¡Hola! Estaba esperándote."
            else "¡Hola! Soy $petName y estoy listo para hoy.",
        )

        PetSpeechState.IDLE_ENCOURAGEMENT -> PetMessage(
            state,
            "Recuérdame si necesitas un empujón. ¡Tú puedes!",
        )

        PetSpeechState.MISSION_JUST_COMPLETED -> PetMessage(
            state,
            "¡Misión completada! Has ganado $xpGained XP.",
            priority = 7,
        )

        PetSpeechState.ALL_DONE_CELEBRATION -> PetMessage(
            state,
            "¡Todo listo por hoy! A descansar, te lo has ganado.",
            priority = 10,
        )

        PetSpeechState.RESTING -> PetMessage(
            state,
            "Zzz... avísame cuando vuelvas a estar por aquí.",
        )
    }

    /** Rotating motivational line for the home screen. */
    fun motivationalLine(seed: Int): String = motivationalMessages[
        (seed % motivationalMessages.size + motivationalMessages.size) % motivationalMessages.size
    ]

    val motivationalMessages: List<String> = listOf(
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
