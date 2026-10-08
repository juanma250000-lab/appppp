package com.mision.app.presentation.celebrations

import com.mision.app.core.text.plural
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.usecase.MissionCompletionResult
import com.mision.app.notifications.MisionNotifier
import kotlinx.coroutines.flow.first

/** UI payload of a celebration dialog. Always in Spanish. */
data class CelebrationUi(
    val emoji: String,
    val title: String,
    val message: String,
    val details: List<String> = emptyList(),
)

/**
 * Picks the most meaningful celebration of a mission completion and gathers
 * every secondary reward as detail lines, so a single dialog can explain
 * everything that just happened.
 */
fun MissionCompletionResult.toCelebrationUi(): CelebrationUi {
    val milestoneEvent = milestone
    val details = buildList {
        if (bonusXp > 0) add("+$bonusXp XP extra")
        if (bonusCoins > 0) add("+${plural(bonusCoins, "moneda", "monedas")} extra")
        newAchievements.forEach { add("Logro: ${it.name}") }
        if (perfectDay && !leveledUp && milestoneEvent == null && newAchievements.isEmpty()) {
            add("Todas las misiones de hoy completadas")
        }
    }

    return when {
        leveledUp -> CelebrationUi(
            emoji = "🚀",
            title = "¡Subiste de nivel!",
            message = "Ahora eres nivel $newLevel. Sigue así y llegarás al ${(newLevel + 1).coerceAtMost(60)} muy pronto.",
            details = details,
        )

        milestoneEvent != null -> CelebrationUi(
            emoji = "🔥",
            title = milestoneEvent.title,
            message = milestoneEvent.message,
            details = details + "+${milestoneEvent.rewardXp} XP · +${milestoneEvent.rewardCoins} monedas",
        )

        perfectDay -> CelebrationUi(
            emoji = "🌟",
            title = "¡Día completado!",
            message = "Has cerrado todas las misiones de hoy. Tu mascota está encantada.",
            details = details,
        )

        newAchievements.isNotEmpty() -> CelebrationUi(
            emoji = "🏆",
            title = "¡Logro desbloqueado!",
            message = newAchievements.joinToString("\n") { "• ${it.name}: ${it.description}" },
            details = details,
        )

        else -> CelebrationUi(
            emoji = "✨",
            title = "¡Misión completada!",
            message = "Has ganado $xpGained XP y ${plural(coinsGained, "moneda", "monedas")}.",
            details = details,
        )
    }
}

/**
 * Bridges the domain results with the notification layer: celebrations are
 * only posted when the user enabled notifications, and they never crash the
 * flow if permission is missing.
 */
class CelebrationDispatcher(
    private val notifier: MisionNotifier,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun dispatch(result: MissionCompletionResult) {
        val enabled = settingsRepository.settings.first().notificationsEnabled
        if (!enabled) return
        if (result.leveledUp) notifier.showLevelUp(result.newLevel)
        result.newAchievements.forEach { notifier.showAchievement(it.name) }
        result.milestone?.let { notifier.showStreakMilestone(it.days) }
    }
}
