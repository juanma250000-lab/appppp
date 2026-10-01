package com.mision.app.domain.model

/** Emotional state of the pet, driven by user activity. */
enum class PetMood(val displayName: String, val emoji: String) {
    FELIZ("Feliz", "😊"),
    MOTIVADO("Motivado", "🔥"),
    CANSADO("Cansado", "🥱"),
    TRISTE("Triste", "🌧️"),
    ORGULLOSO("Orgulloso", "🥹"),
    CELEBRANDO("Celebrando", "🎉"),
}

/** Slots a cosmetic item can occupy. */
enum class CosmeticSlot(val displayName: String) {
    HAT("Sombreros"),
    ACCESSORY("Accesorios"),
    BACKGROUND("Fondos"),
    COLOR("Colores"),
    EFFECT("Efectos"),
    EMOTE("Emotes"),
    SKIN("Pieles"),
}

/** Which cosmetics are currently worn by the pet. */
data class EquippedCosmetics(
    val hat: String? = null,
    val accessory: String? = null,
    val background: String? = null,
    val color: String? = null,
    val effect: String? = null,
    val emote: String? = null,
    val skin: String? = null,
) {
    fun idFor(slot: CosmeticSlot): String? = when (slot) {
        CosmeticSlot.HAT -> hat
        CosmeticSlot.ACCESSORY -> accessory
        CosmeticSlot.BACKGROUND -> background
        CosmeticSlot.COLOR -> color
        CosmeticSlot.EFFECT -> effect
        CosmeticSlot.EMOTE -> emote
        CosmeticSlot.SKIN -> skin
    }

    fun with(slot: CosmeticSlot, itemId: String?): EquippedCosmetics = when (slot) {
        CosmeticSlot.HAT -> copy(hat = itemId)
        CosmeticSlot.ACCESSORY -> copy(accessory = itemId)
        CosmeticSlot.BACKGROUND -> copy(background = itemId)
        CosmeticSlot.COLOR -> copy(color = itemId)
        CosmeticSlot.EFFECT -> copy(effect = itemId)
        CosmeticSlot.EMOTE -> copy(emote = itemId)
        CosmeticSlot.SKIN -> copy(skin = itemId)
    }

    fun encode(): String = listOfNotNull(
        hat?.let { "hat:$it" },
        accessory?.let { "acc:$it" },
        background?.let { "bg:$it" },
        color?.let { "color:$it" },
        effect?.let { "fx:$it" },
        emote?.let { "emote:$it" },
        skin?.let { "skin:$it" },
    ).joinToString(";")

    companion object {
        private val prefixToSlot = mapOf(
            "hat" to CosmeticSlot.HAT,
            "acc" to CosmeticSlot.ACCESSORY,
            "bg" to CosmeticSlot.BACKGROUND,
            "color" to CosmeticSlot.COLOR,
            "fx" to CosmeticSlot.EFFECT,
            "emote" to CosmeticSlot.EMOTE,
            "skin" to CosmeticSlot.SKIN,
        )

        fun decode(raw: String): EquippedCosmetics {
            var result = EquippedCosmetics()
            if (raw.isBlank()) return result
            raw.split(";").forEach { part ->
                val separator = part.indexOf(':')
                if (separator <= 0) return@forEach
                val slot = prefixToSlot[part.substring(0, separator)] ?: return@forEach
                val value = part.substring(separator + 1)
                if (value.isNotBlank()) result = result.with(slot, value)
            }
            return result
        }
    }
}

/**
 * The virtual pet. Happiness/energy are 0..100 percentages; XP feeds the pet
 * level which is derived through [com.mision.app.core.gamification.LevelCalculator].
 */
data class Pet(
    val name: String,
    val xp: Int,
    val happiness: Int,
    val energy: Int,
    val mood: PetMood,
    val equipped: EquippedCosmetics,
    val lastInteractionEpochDay: Int,
) {
    val level: Int get() = com.mision.app.core.gamification.LevelCalculator.levelFor(xp)
    val levelProgress: com.mision.app.core.gamification.LevelProgress
        get() = com.mision.app.core.gamification.LevelCalculator.progressFor(xp)

    companion object {
        fun default(name: String, epochDay: Int) = Pet(
            name = name,
            xp = 0,
            happiness = 70,
            energy = 80,
            mood = PetMood.MOTIVADO,
            equipped = EquippedCosmetics(),
            lastInteractionEpochDay = epochDay,
        )
    }
}
