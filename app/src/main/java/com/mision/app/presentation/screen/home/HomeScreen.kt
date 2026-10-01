package com.mision.app.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.AppSettings
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.CelebrationDialog
import com.mision.app.presentation.components.CoinPill
import com.mision.app.presentation.components.EmptyState
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassIconButton
import com.mision.app.presentation.components.GradientProgressBar
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.MissionCard
import com.mision.app.presentation.components.PetSpeechBubble
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.StreakCard
import com.mision.app.presentation.components.TintedGlassSurface
import com.mision.app.presentation.components.XpIndicator
import com.mision.app.presentation.celebrations.CelebrationDispatcher
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.HomeViewModel

/**
 * Inicio: greeting, pet hero, progression, streak and today's missions.
 */
@Composable
fun HomeScreen(
    onOpenMissions: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPet: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            useCases = container.useCases,
            progressRepository = container.progressRepository,
            gamificationRepository = container.gamificationRepository,
            petRepository = container.petRepository,
            clock = container.clock,
            celebrationDispatcher = CelebrationDispatcher(
                notifier = container.notifier,
                settingsRepository = container.settingsRepository,
            ),
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val settings by container.settingsRepository.settings
        .collectAsStateWithLifecycle(initialValue = AppSettings())
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) { viewModel.refresh() }

    celebration?.let { dialog ->
        CelebrationDialog(
            emoji = dialog.emoji,
            title = dialog.title,
            message = dialog.message,
            onDismiss = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.dismissCelebration()
            },
            confirmLabel = "¡Seguimos!",
        )
    }

    MisionScreen(
        title = if (state.greeting.isEmpty()) "Misión" else state.greeting,
        subtitle = state.dateLabel,
        actions = {
            CoinPill(coins = state.profile.coins)
            GlassIconButton(
                imageVector = Icons.Filled.Storefront,
                contentDescription = "Abrir la tienda",
                onClick = onOpenShop,
            )
            GlassIconButton(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Abrir ajustes",
                onClick = onOpenSettings,
            )
        },
    ) {
        PetHero(
            petName = state.pet.name,
            speech = state.speech,
            pet = state.pet,
            animate = settings.animationsEnabled,
            onClick = onOpenPet,
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tu progreso",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CoinPill(coins = state.profile.coins)
                }
                XpIndicator(
                    level = state.profile.level,
                    progress = state.profile.levelProgress.progress,
                    xpIntoLevel = state.profile.levelProgress.xpIntoLevel,
                    xpToNext = state.profile.levelProgress.xpToNext,
                )
            }
        }

        StreakCard(streak = state.streak)

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                SectionHeader(
                    title = "Misiones de hoy",
                    actionLabel = "Ver todas",
                    onAction = onOpenMissions,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${state.completedToday} de ${state.totalToday} completadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(state.progress * 100).toInt()} %",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                GradientProgressBar(
                    progress = state.progress,
                    colors = if (state.allCompleted) AppGradients.celebrate else AppGradients.primary,
                    label = "Progreso de hoy",
                )

                if (state.missions.isEmpty()) {
                    EmptyState(
                        emoji = "🌱",
                        title = "Aún no hay misiones",
                        message = "Crea tu primera misión y empieza a construir tu racha.",
                        actionLabel = "Crear misión",
                        onAction = onOpenMissions,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                        state.missions.take(HOME_MISSION_LIMIT).forEach { mission ->
                            MissionCard(
                                mission = mission,
                                onToggle = {
                                    if (!mission.isCompleted) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.onToggleMission(mission)
                                },
                                onClick = onOpenMissions,
                            )
                        }
                    }
                    if (state.missions.size > HOME_MISSION_LIMIT) {
                        GlassButton(
                            text = "Ver las ${state.missions.size} misiones",
                            onClick = onOpenMissions,
                            style = GlassButtonStyle.TONAL,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (state.allCompleted && state.totalToday > 0) {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎉", style = MaterialTheme.typography.headlineMedium)
                    Column(modifier = Modifier.padding(start = Dimens.SpaceMd)) {
                        Text(
                            text = "¡Día completado!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Mañana te esperan nuevas misiones. Descansa tranquilo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Text(
            text = state.motivationalLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.SpaceSm),
        )
    }
}

@Composable
private fun PetHero(
    petName: String,
    speech: String,
    pet: com.mision.app.domain.model.Pet,
    animate: Boolean,
    onClick: () -> Unit,
) {
    TintedGlassSurface(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            AnimatedPet(
                pet = pet,
                size = 150.dp,
                animate = animate,
            )
            PetSpeechBubble(
                text = speech,
                petName = petName,
                modifier = Modifier.fillMaxWidth(),
            )
            GlassButton(
                text = "Ver a $petName",
                onClick = onClick,
                style = GlassButtonStyle.TONAL,
                height = Dimens.ButtonHeightCompact,
            )
        }
    }
}

private const val HOME_MISSION_LIMIT = 4
