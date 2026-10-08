package com.mision.app.presentation.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.core.time.DateFormats
import com.mision.app.domain.model.DayStat
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AchievementCard
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassIconButton
import com.mision.app.presentation.components.GradientProgressBar
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.StatTile
import com.mision.app.presentation.components.StreakCard
import com.mision.app.presentation.components.XpIndicator
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.ProfileViewModel
import java.time.LocalDate

/**
 * Perfil: identity, level, weekly/monthly statistics and the achievement
 * catalogue.
 */
@Composable
fun ProfileScreen(onOpenSettings: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            useCases = container.useCases,
            progressRepository = container.progressRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Stats change while the user is on other tabs, so reload on every visit.
    LaunchedEffect(Unit) { viewModel.refresh() }

    state.infoMessage?.let { message ->
        ConfirmDialog(
            title = "Perfil",
            message = message,
            confirmLabel = "Entendido",
            onConfirm = viewModel::dismissInfo,
            onDismiss = viewModel::dismissInfo,
            dismissLabel = "Cerrar",
        )
    }

    MisionScreen(
        title = "Perfil",
        subtitle = state.stats?.let { "Nivel ${it.profile.level} · ${it.profile.name}" },
        actions = {
            GlassIconButton(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Editar nombre",
                onClick = viewModel::startEditName,
            )
            GlassIconButton(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Abrir ajustes",
                onClick = onOpenSettings,
            )
        },
    ) {
        // ---- Identity ----------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                if (state.isEditingName) {
                    OutlinedTextField(
                        value = state.nameDraft.orEmpty(),
                        onValueChange = viewModel::onNameDraft,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tu nombre") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        ),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                        GlassButton(
                            text = "Guardar",
                            onClick = viewModel::saveName,
                            modifier = Modifier.weight(1f),
                            height = Dimens.ButtonHeightCompact,
                        )
                        GlassButton(
                            text = "Cancelar",
                            onClick = viewModel::cancelEditName,
                            style = GlassButtonStyle.TONAL,
                            modifier = Modifier.weight(1f),
                            height = Dimens.ButtonHeightCompact,
                        )
                    }
                } else {
                    Text(
                        text = state.profile.name.ifBlank { "Viajero" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    XpIndicator(
                        level = state.profile.level,
                        progress = state.profile.levelProgress.progress,
                        xpIntoLevel = state.profile.levelProgress.xpIntoLevel,
                        xpToNext = state.profile.levelProgress.xpToNext,
                    )
                }
            }
        }

        // ---- Stats --------------------------------------------------------
        val stats = state.stats
        if (stats != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                StatTile(
                    emoji = "✅",
                    label = "Misiones",
                    value = stats.profile.totalMissionsCompleted.toString(),
                    gradient = AppGradients.primary,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    emoji = "🪙",
                    label = "Monedas",
                    value = stats.profile.coins.toString(),
                    gradient = AppGradients.coin,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    emoji = "🏆",
                    label = "Logros",
                    value = "${stats.unlockedAchievements}/${stats.totalAchievements}",
                    gradient = AppGradients.celebrate,
                    modifier = Modifier.weight(1f),
                )
            }

            StreakCard(streak = stats.streak)

            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                    SectionHeader(title = "Esta semana")
                    WeeklyChart(days = stats.weekStats)
                    Text(
                        text = "Misiones completadas este mes: ${stats.monthCompleted} de ${stats.monthGoalDays} días.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                    ) {
                        StatTile(
                            emoji = "📅",
                            label = "Mes",
                            value = stats.monthCompleted.toString(),
                            gradient = AppGradients.primary,
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            emoji = "🌟",
                            label = "Días perfectos",
                            value = state.achievementStats.perfectDays.toString(),
                            gradient = AppGradients.celebrate,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // ---- Achievements --------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Logros")
                Text(
                    text = "Completa misiones, mantén tu racha y sube de nivel para desbloquearlos todos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.achievements.isEmpty()) {
                    Text(
                        text = "Cargando logros…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.achievements.forEach { achievement ->
                        AchievementCard(
                            achievement = achievement,
                            currentValue = viewModel.currentValueFor(achievement),
                            target = achievement.definition.target,
                        )
                    }
                }
            }
        }
    }
}

/** Seven column chart with Spanish weekday initials. */
@Composable
private fun WeeklyChart(days: List<DayStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                val date = LocalDate.ofEpochDay(day.epochDay.toLong())
                val ratio = if (day.total <= 0) 0f else day.completed.toFloat() / day.total
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                ) {
                    Text(
                        text = "${day.completed}/${day.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GradientProgressBar(
                        progress = ratio,
                        colors = if (ratio >= 1f && day.total > 0) {
                            AppGradients.celebrate
                        } else {
                            AppGradients.primary
                        },
                        height = 72.dp,
                        modifier = Modifier.fillMaxWidth(),
                        label = "Misiones del ${DateFormats.shortDate(date)}",
                    )
                    Text(
                        text = DateFormats.dayInitial(date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
