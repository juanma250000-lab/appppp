package com.mision.app.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.Mission
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.CelebrationDialog
import com.mision.app.presentation.components.CoinPill
import com.mision.app.presentation.components.EmptyState
import com.mision.app.presentation.components.HeroSurface
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionCard
import com.mision.app.presentation.components.MisionIconButton
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.MissionCard
import com.mision.app.presentation.components.OnResumeEffect
import com.mision.app.presentation.components.PetSpeechBubble
import com.mision.app.presentation.components.ProgressBar
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.SnackbarMessageEffect
import com.mision.app.presentation.components.StreakCard
import com.mision.app.presentation.components.XpIndicator
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors
import com.mision.app.presentation.viewmodel.HomeUiState
import com.mision.app.presentation.viewmodel.HomeViewModel

/** Inicio: greeting, pet, progression, streak and today's missions. */
@Composable
fun HomeScreen(
    onOpenMissions: () -> Unit,
    onOpenPet: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            useCases = container.useCases,
            progressRepository = container.progressRepository,
            gamificationRepository = container.gamificationRepository,
            petRepository = container.petRepository,
            clock = container.clock,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val message by viewModel.messages.current.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current

    OnResumeEffect(viewModel::refresh)
    SnackbarMessageEffect(message, snackbar, viewModel.messages::consumed)

    celebration?.let {
        CelebrationDialog(
            celebration = it,
            onDismiss = viewModel::dismissCelebration,
            confirmLabel = "¡Seguimos!",
        )
    }

    MisionScreen(
        title = state.greeting.ifEmpty { "Misión" },
        subtitle = state.dateLabel,
        snackbarHostState = snackbar,
        actions = {
            MisionIconButton(
                icon = Icons.Filled.Storefront,
                contentDescription = "Abrir la tienda",
                onClick = onOpenShop,
            )
            MisionIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Abrir ajustes",
                onClick = onOpenSettings,
            )
        },
    ) {
        PetHero(state = state, onOpenPet = onOpenPet)
        ProgressCard(state = state)
        StreakCard(streak = state.streak)
        TodayCard(
            state = state,
            onOpenMissions = onOpenMissions,
            onToggle = { mission ->
                if (!mission.isCompleted) haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                viewModel.onToggleMission(mission)
            },
        )
        Text(
            text = state.motivationalLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PetHero(state: HomeUiState, onOpenPet: () -> Unit) {
    HeroSurface(modifier = Modifier.fillMaxWidth()) {
        AnimatedPet(pet = state.pet, size = Dimens.PetHero)
        PetSpeechBubble(text = state.speech, petName = state.pet.name)
        MisionButton(
            text = "Ver a ${state.pet.name}",
            onClick = onOpenPet,
            style = MisionButtonStyle.TONAL,
            height = Dimens.ButtonHeightCompact,
            modifier = Modifier.padding(top = Dimens.SpaceMd),
        )
    }
}

@Composable
private fun ProgressCard(state: HomeUiState) {
    MisionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(title = "Tu progreso", modifier = Modifier.weight(1f))
            CoinPill(coins = state.profile.coins)
        }
        XpIndicator(
            level = state.profile.level,
            progress = state.profile.levelProgress.progress,
            xpIntoLevel = state.profile.levelProgress.xpIntoLevel,
            xpToNext = state.profile.levelProgress.xpToNext,
            modifier = Modifier.padding(top = Dimens.SpaceMd),
        )
    }
}

@Composable
private fun TodayCard(
    state: HomeUiState,
    onOpenMissions: () -> Unit,
    onToggle: (Mission) -> Unit,
) {
    MisionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Misiones de hoy",
            actionLabel = "Ver todas",
            onAction = onOpenMissions,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.SpaceSm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (state.allCompleted) "¡Todas completadas!" else "${state.completedToday} de ${state.totalToday} completadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${(state.progress * 100).toInt()} %",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        ProgressBar(
            progress = state.progress,
            label = "Progreso de hoy: ${state.completedToday} de ${state.totalToday}",
            color = if (state.allCompleted) MisionColors.game.success else MaterialTheme.colorScheme.primary,
        )

        if (!state.isLoading && state.missions.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Spa,
                title = "Aún no hay misiones",
                message = "Crea tu primera misión y empieza a construir tu racha.",
                actionLabel = "Crear misión",
                actionIcon = Icons.Filled.Add,
                onAction = onOpenMissions,
                modifier = Modifier.padding(top = Dimens.SpaceLg),
            )
        } else {
            Column(
                modifier = Modifier.padding(top = Dimens.SpaceMd),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                state.missions.take(HOME_MISSION_LIMIT).forEach { mission ->
                    MissionCard(mission = mission, onToggle = { onToggle(mission) })
                }
            }
            if (state.missions.size > HOME_MISSION_LIMIT) {
                MisionButton(
                    text = "Ver las ${state.missions.size} misiones",
                    onClick = onOpenMissions,
                    style = MisionButtonStyle.OUTLINE,
                    height = Dimens.ButtonHeightCompact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpaceMd),
                )
            }
        }
    }
}

private const val HOME_MISSION_LIMIT = 4
