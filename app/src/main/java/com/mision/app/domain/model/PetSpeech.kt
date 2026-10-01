package com.mision.app.domain.model

/**
 * Reusable states of the pet speech bubble.
 *
 * Every contextual message of the app is declared here so copy stays
 * consistent, translatable and testable instead of being scattered through
 * composables.
 */
enum class PetSpeechState {
    ZERO_PROGRESS,
    EARLY_PROGRESS,
    HALF_PROGRESS,
    NEAR_COMPLETE,
    DAY_COMPLETE,
    MORNING,
    AFTERNOON,
    EVENING,
    STREAK_MILESTONE,
    LEVEL_UP,
    ACHIEVEMENT,
    FIRST_OPEN,
    IDLE_ENCOURAGEMENT,
    MISSION_JUST_COMPLETED,
    ALL_DONE_CELEBRATION,
    RESTING,
}

/** A single line the pet can say. */
data class PetMessage(
    val state: PetSpeechState,
    val text: String,
    val priority: Int = 0,
)
