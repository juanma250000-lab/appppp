package com.mision.app.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.core.time.DateFormats
import com.mision.app.domain.model.DayStat
import com.mision.app.domain.model.ProfileStats
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AchievementCard
import com.mision.app.presentation.components.IconBadge
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionCard
import com.mision.app.presentation.components.MisionIconButton
import com.mision.app.presentation.components.MisionIcons
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.OnResumeEffect
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.SnackbarMessageEffect
import com.mision.app.presentation.components.StatTile
import com.mision.app.presentation.components.StreakCard
import com.mision.app.presentation.components.XpIndicator
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors
import com.mision.app.presentation.viewmodel.ProfileUiState
import com.mision.app.presentation.viewmodel.ProfileViewModel
import java.time.LocalDate

/** Perfil: identity, level, weekly/monthly statistics and achievements. */
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
    val message by viewModel.messages.current.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    OnResumeEffect(viewModel::refresh)
    SnackbarMessageEffect(message, snackbar, viewModel.messages::consumed)

    MisionScreen(
        title = "Perfil",
        subtitle = "Nivel ${state.profile.level} · ${state.profile.name}",
        snackbarHostState = snackbar,
        actions = {
            MisionIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Abrir ajustes",
                onClick = onOpenSettings,
            )
        },
    ) {
        IdentityCard(
            state = state,
            onEdit = viewModel::startEditName,
            onDraft = viewModel::onNameDraft,
            onSave = viewModel::saveName,
            onCancel = viewModel::cancelEditName,
        )

        state.stats?.let { stats ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                StatTile(
                    icon = Icons.Filled.CheckCircle,
                    label = "Misiones",
                    value = stats.profile.totalMissionsCompleted.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    icon = MisionIcons.Coin,
                    label = "Monedas",
                    value = stats.profile.coins.toString(),
                    iconContainer = MisionColors.game.coin,
                    iconColor = MisionColors.game.onCoin,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    icon = Icons.Filled.EmojiEvents,
                    label = "Logros",
                    value = "${stats.unlockedAchievements}/${stats.totalAchievements}",
                    iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
            StreakCard(streak = stats.streak)
            WeekCard(stats = stats)
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Logros")
            Text(
                text = "Completa misiones, mantén tu racha y sube de nivel para desbloquearlos todos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.achievements.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AchievementCard(item = item, modifier = Modifier.padding(vertical = Dimens.SpaceMd))
            }
        }
    }
}

@Composable
private fun IdentityCard(
    state: ProfileUiState,
    onEdit: () -> Unit,
    onDraft: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    MisionCard(modifier = Modifier.fillMaxWidth()) {
        val draft = state.nameDraft
        if (draft != null) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraft,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tu nombre") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSave() }),
            )
            Row(
                modifier = Modifier.padding(top = Dimens.SpaceMd),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                MisionButton(
                    text = "Cancelar",
                    onClick = onCancel,
                    style = MisionButtonStyle.OUTLINE,
                    height = Dimens.ButtonHeightCompact,
                    modifier = Modifier.weight(1f),
                )
                MisionButton(
                    text = "Guardar",
                    onClick = onSave,
                    height = Dimens.ButtonHeightCompact,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                MisionIconButton(icon = Icons.Filled.Edit, contentDescription = "Editar nombre", onClick = onEdit)
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
}

@Composable
private fun WeekCard(stats: ProfileStats) {
    MisionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Últimos 7 días")
        WeeklyChart(days = stats.weekStats, modifier = Modifier.padding(vertical = Dimens.SpaceMd))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.padding(top = Dimens.SpaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            InlineMetric(
                icon = Icons.Filled.CalendarMonth,
                label = "Misiones este mes",
                value = stats.monthCompleted.toString(),
                modifier = Modifier.weight(1f),
            )
            InlineMetric(
                icon = Icons.Filled.WorkspacePremium,
                label = "Días perfectos",
                value = stats.perfectDays.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Icon + value + label without its own card (used inside a card). */
@Composable
private fun InlineMetric(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        IconBadge(
            icon = icon,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Seven vertical bars (completed / planned) with Spanish weekday initials. */
@Composable
private fun WeeklyChart(days: List<DayStat>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val success = MisionColors.game.success
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        days.forEach { day ->
            val date = LocalDate.ofEpochDay(day.epochDay.toLong())
            val complete = day.total > 0 && day.completed >= day.total
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "${DateFormats.shortDate(date)}: ${day.completed} de ${day.total} misiones"
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                Text(
                    text = day.completed.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.WeeklyChartHeight)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(colors.surfaceContainerHighest),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(day.progress)
                            .background(if (complete) success else colors.primary),
                    )
                }
                Text(
                    text = DateFormats.dayInitial(date),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface,
                )
            }
        }
    }
}
