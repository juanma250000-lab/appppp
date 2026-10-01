package com.mision.app.core.gamification

import com.mision.app.domain.model.AchievementDefinition
import com.mision.app.domain.model.AchievementIcon
import com.mision.app.domain.model.AchievementStats
import com.mision.app.domain.model.AchievementType

/**
 * Every achievement of the game. Content and rewards live here only, so new
 * achievements can be added without touching evaluation or UI code.
 */
object AchievementCatalog {

    val all: List<AchievementDefinition> = listOf(
        AchievementDefinition(
            id = "primer_paso",
            name = "Primer paso",
            description = "Completa tu primera misión.",
            icon = AchievementIcon.CHECK,
            type = AchievementType.FIRST_MISSION,
            target = 1,
            rewardXp = 50,
            rewardCoins = 10,
        ),
        AchievementDefinition(
            id = "en_marcha",
            name = "En marcha",
            description = "Completa 10 misiones.",
            icon = AchievementIcon.STAR,
            type = AchievementType.TOTAL_MISSIONS,
            target = 10,
            rewardXp = 100,
            rewardCoins = 30,
        ),
        AchievementDefinition(
            id = "maestro_de_habitos",
            name = "Maestro de hábitos",
            description = "Completa 100 misiones.",
            icon = AchievementIcon.MEDAL,
            type = AchievementType.TOTAL_MISSIONS,
            target = 100,
            rewardXp = 800,
            rewardCoins = 300,
        ),
        AchievementDefinition(
            id = "constancia",
            name = "Constancia",
            description = "Completa misiones durante 7 días seguidos.",
            icon = AchievementIcon.FLAME,
            type = AchievementType.STREAK_DAYS,
            target = 7,
            rewardXp = 200,
            rewardCoins = 60,
        ),
        AchievementDefinition(
            id = "disciplina",
            name = "Disciplina",
            description = "Alcanza una racha de 30 días.",
            icon = AchievementIcon.TROPHY,
            type = AchievementType.STREAK_DAYS,
            target = 30,
            rewardXp = 600,
            rewardCoins = 200,
        ),
        AchievementDefinition(
            id = "imparable",
            name = "Imparable",
            description = "Alcanza una racha de 100 días.",
            icon = AchievementIcon.CROWN,
            type = AchievementType.STREAK_DAYS,
            target = 100,
            rewardXp = 1500,
            rewardCoins = 500,
        ),
        AchievementDefinition(
            id = "inquebrantable",
            name = "Inquebrantable",
            description = "Mantén una racha de 365 días.",
            icon = AchievementIcon.BOLT,
            type = AchievementType.STREAK_DAYS,
            target = 365,
            rewardXp = 6000,
            rewardCoins = 2000,
        ),
        AchievementDefinition(
            id = "completista",
            name = "Completista",
            description = "Completa todas las misiones de un día.",
            icon = AchievementIcon.CHECK,
            type = AchievementType.PERFECT_DAY,
            target = 1,
            rewardXp = 150,
            rewardCoins = 50,
        ),
        AchievementDefinition(
            id = "jornada_perfecta",
            name = "Jornada perfecta",
            description = "Consigue 10 días con todas las misiones completadas.",
            icon = AchievementIcon.SPARKLE,
            type = AchievementType.PERFECT_DAY,
            target = 10,
            rewardXp = 700,
            rewardCoins = 250,
        ),
        AchievementDefinition(
            id = "ascenso",
            name = "Ascenso",
            description = "Alcanza el nivel 5.",
            icon = AchievementIcon.HEART,
            type = AchievementType.LEVEL_REACHED,
            target = 5,
            rewardXp = 300,
            rewardCoins = 100,
        ),
        AchievementDefinition(
            id = "elite",
            name = "Élite",
            description = "Alcanza el nivel 15.",
            icon = AchievementIcon.BOOK,
            type = AchievementType.LEVEL_REACHED,
            target = 15,
            rewardXp = 1200,
            rewardCoins = 400,
        ),
        AchievementDefinition(
            id = "cofre_lleno",
            name = "Cofre lleno",
            description = "Acumula 1000 monedas ganadas.",
            icon = AchievementIcon.MEDAL,
            type = AchievementType.COINS_EARNED,
            target = 1000,
            rewardXp = 250,
            rewardCoins = 0,
        ),
        AchievementDefinition(
            id = "coleccionista",
            name = "Coleccionista",
            description = "Compra 5 recompensas en la tienda.",
            icon = AchievementIcon.STAR,
            type = AchievementType.SHOP_PURCHASES,
            target = 5,
            rewardXp = 200,
            rewardCoins = 50,
        ),
    )

    fun byId(id: String): AchievementDefinition? = all.firstOrNull { it.id == id }
}

/** Pure evaluation: which achievements unlock for the given snapshot. */
object AchievementEvaluator {

    fun newlyUnlocked(
        stats: AchievementStats,
        alreadyUnlocked: Set<String>,
    ): List<AchievementDefinition> = AchievementCatalog.all.filter { definition ->
        definition.id !in alreadyUnlocked && definition.isUnlocked(stats)
    }
}
