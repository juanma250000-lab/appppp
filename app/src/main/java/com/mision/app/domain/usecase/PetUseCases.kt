package com.mision.app.domain.usecase

import com.mision.app.core.gamification.PetMoodCalculator
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository

/**
 * Pet side-effects shared by missions, streaks and the pet screen.
 *
 * Keeping them here (instead of inside a repository) guarantees the mood is
 * always derived from [PetMoodCalculator] with the same inputs.
 */
internal object PetEffects {

    /** The pet reacts to a completed mission: XP, happiness and mood. */
    suspend fun onMissionCompleted(
        petRepository: PetRepository,
        todayEpochDay: Int,
        completionRatio: Float,
        allCompleted: Boolean,
        newStreakRecord: Boolean,
        xpGained: Int,
    ): Pet {
        val pet = petRepository.getPet()
        val happiness = (pet.happiness + 4).coerceIn(0, 100)
        val mood = PetMoodCalculator.calculate(
            completionRatio = completionRatio,
            energy = pet.energy,
            happiness = happiness,
            daysSinceInteraction = 0,
            allCompleted = allCompleted,
            newStreakRecord = newStreakRecord,
            momentaryBoost = allCompleted || newStreakRecord,
        )
        val updated = pet.copy(
            xp = (pet.xp + xpGained).coerceAtLeast(0),
            happiness = happiness,
            mood = mood,
            lastInteractionEpochDay = todayEpochDay,
        )
        petRepository.savePet(updated)
        return updated
    }

    /** Recomputes the resting mood from the current state, without XP gain. */
    suspend fun refresh(
        petRepository: PetRepository,
        todayEpochDay: Int,
        completionRatio: Float,
        allCompleted: Boolean,
    ): PetMood {
        val pet = petRepository.getPet()
        val daysSince = (todayEpochDay - pet.lastInteractionEpochDay).coerceAtLeast(0)
        val mood = PetMoodCalculator.calculate(
            completionRatio = completionRatio,
            energy = pet.energy,
            happiness = pet.happiness,
            daysSinceInteraction = daysSince,
            allCompleted = allCompleted,
            newStreakRecord = false,
        )
        if (mood != pet.mood) petRepository.savePet(pet.copy(mood = mood))
        return mood
    }
}

/** Actions available on the pet screen, with Spanish labels. */
enum class PetAction(val displayName: String, val energyDelta: Int, val happinessDelta: Int) {
    ALIMENTAR("Alimentar", energyDelta = 12, happinessDelta = 5),
    JUGAR("Jugar", energyDelta = -8, happinessDelta = 12),
    DESCANSAR("Descansar", energyDelta = 18, happinessDelta = 2),
}

data class PetInteractionResult(
    val pet: Pet,
    val mood: PetMood,
    val message: String,
)

/**
 * Recomputes the pet mood from the live state. Called when the app resumes or
 * a screen showing the pet becomes visible, so inactivity is always reflected.
 */
class UpdatePetMoodUseCase(
    private val petRepository: PetRepository,
    private val missionRepository: MissionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(): PetMood {
        val today = clock.todayEpochDay()
        val (pending, total) = missionRepository.countForDay(today)
        val ratio = if (total <= 0) 0f else (total - pending).toFloat() / total
        return PetEffects.refresh(
            petRepository = petRepository,
            todayEpochDay = today,
            completionRatio = ratio,
            allCompleted = total > 0 && pending == 0,
        )
    }
}

/** Feeds, plays with or rests the pet, clamping stats to 0..100. */
class InteractWithPetUseCase(
    private val petRepository: PetRepository,
    private val missionRepository: MissionRepository,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(action: PetAction, petName: String): PetInteractionResult {
        val today = clock.todayEpochDay()
        val pet = petRepository.getPet()
        val happiness = (pet.happiness + action.happinessDelta).coerceIn(0, 100)
        val energy = (pet.energy + action.energyDelta).coerceIn(0, 100)

        val (pending, total) = missionRepository.countForDay(today)
        val ratio = if (total <= 0) 0f else (total - pending).toFloat() / total
        val allCompleted = total > 0 && pending == 0

        val mood = PetMoodCalculator.calculate(
            completionRatio = ratio,
            energy = energy,
            happiness = happiness,
            daysSinceInteraction = 0,
            allCompleted = allCompleted,
            newStreakRecord = false,
            momentaryBoost = action == PetAction.JUGAR && happiness >= 70,
        )

        val updated = pet.copy(
            happiness = happiness,
            energy = energy,
            mood = mood,
            lastInteractionEpochDay = today,
        )
        petRepository.savePet(updated)

        return PetInteractionResult(
            pet = updated,
            mood = mood,
            message = messageFor(action, petName),
        )
    }

    private fun messageFor(action: PetAction, petName: String): String = when (action) {
        PetAction.ALIMENTAR -> "$petName come feliz y recupera energía."
        PetAction.JUGAR -> "¡Has jugado con $petName! Se nota la alegría."
        PetAction.DESCANSAR -> "$petName descansa y vuelve con más energía."
    }
}
