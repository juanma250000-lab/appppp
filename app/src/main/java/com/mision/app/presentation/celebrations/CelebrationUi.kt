package com.mision.app.presentation.celebrations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.ui.graphics.vector.ImageVector
import com.mision.app.core.gamification.LevelCalculator
import com.mision.app.domain.usecase.MissionCompletionResult

/** What is being celebrated; drives the dialog icon. */
enum class CelebrationKind { LEVEL_UP, STREAK, PERFECT_DAY, ACHIEVEMENT, MISSION }

val CelebrationKind.icon: ImageVector
    get() = when (this) {
        CelebrationKind.LEVEL_UP -> Icons.Filled.MilitaryTech
        CelebrationKind.STREAK -> Icons.Filled.LocalFireDepartment
        CelebrationKind.PERFECT_DAY -> Icons.Filled.Celebration
        CelebrationKind.ACHIEVEMENT -> Icons.Filled.EmojiEvents
        CelebrationKind.MISSION -> Icons.Filled.CheckCircle
    }

/** UI payload of a celebration dialog. Always in Spanish. */
data class CelebrationUi(
    val kind: CelebrationKind,
    val title: String,
    val message: String,
    val details: List<String> = emptyList(),
)

/**
 * Picks the most meaningful celebration of a mission completion and lists
 * every secondary reward once, so a single dialog explains everything that
 * just happened without repeating itself.
 */
fun MissionCompletionResult.toCelebrationUi(): CelebrationUi {
    val milestone = milestone
    val kind = when {
        leveledUp -> CelebrationKind.LEVEL_UP
        milestone != null -> CelebrationKind.STREAK
        perfectDay -> CelebrationKind.PERFECT_DAY
        newAchievements.isNotEmpty() -> CelebrationKind.ACHIEVEMENT
        else -> CelebrationKind.MISSION
    }
    val details = buildList {
        if (milestone != null) {
            // The streak headline already shows the milestone title.
            val prefix = if (kind == CelebrationKind.STREAK) "Bonificación:" else milestone.title
            add("$prefix +${milestone.rewardXp} XP · +${milestone.rewardCoins} monedas")
        }
        if (kind != CelebrationKind.ACHIEVEMENT) newAchievements.forEach { add("Logro: ${it.name}") }
        if (achievementXp > 0 || achievementCoins > 0) {
            add("Logros: +$achievementXp XP · +$achievementCoins monedas")
        }
        if (perfectDay && kind != CelebrationKind.PERFECT_DAY) add("Todas las misiones de hoy completadas")
    }

    return when (kind) {
        CelebrationKind.LEVEL_UP -> CelebrationUi(
            kind = kind,
            title = "¡Subiste de nivel!",
            message = if (newLevel >= LevelCalculator.MAX_LEVEL) {
                "Has llegado al nivel máximo. ¡Leyenda!"
            } else {
                "Ahora eres nivel $newLevel. Sigue así y llegarás al ${newLevel + 1} muy pronto."
            },
            details = details,
        )
        CelebrationKind.STREAK -> CelebrationUi(
            kind = kind,
            title = milestone?.title.orEmpty(),
            message = milestone?.message.orEmpty(),
            details = details,
        )
        CelebrationKind.PERFECT_DAY -> CelebrationUi(
            kind = kind,
            title = "¡Día completado!",
            message = "Has cerrado todas las misiones de hoy. Tu mascota está encantada.",
            details = details,
        )
        CelebrationKind.ACHIEVEMENT -> CelebrationUi(
            kind = kind,
            title = if (newAchievements.size == 1) "¡Logro desbloqueado!" else "¡Logros desbloqueados!",
            message = newAchievements.joinToString("\n") { "${it.name}: ${it.description}" },
            details = details,
        )
        CelebrationKind.MISSION -> CelebrationUi(
            kind = kind,
            title = "¡Misión completada!",
            message = "Has ganado $xpGained XP y $coinsGained monedas.",
            details = details,
        )
    }
}
