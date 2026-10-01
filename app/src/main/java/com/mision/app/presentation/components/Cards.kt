package com.mision.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.usecase.AchievementProgress
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors

/** Section title with an optional text action on the right. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** Empty / error placeholder: icon, explanation and a way out. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        IconBadge(
            icon = icon,
            size = Dimens.IconContainerLg,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            MisionButton(
                text = actionLabel,
                onClick = onAction,
                style = MisionButtonStyle.TONAL,
                leadingIcon = actionIcon,
                height = Dimens.ButtonHeightCompact,
            )
        }
    }
}

/** Compact metric tile (misiones, monedas, logros...). */
@Composable
fun StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    iconContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    MisionCard(
        modifier = modifier.semantics(mergeDescendants = true) {},
        contentPadding = PaddingValues(Dimens.SpaceMd),
    ) {
        IconBadge(
            icon = icon,
            containerColor = iconContainer,
            contentColor = iconColor,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Streak summary: the flame, the current chain and the record. */
@Composable
fun StreakCard(streak: StreakState, modifier: Modifier = Modifier) {
    val game = MisionColors.game
    MisionCard(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            IconBadge(
                icon = Icons.Filled.LocalFireDepartment,
                size = Dimens.IconContainerLg,
                containerColor = game.streakContainer,
                contentColor = game.streak,
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                Text(
                    text = when (streak.currentStreak) {
                        0 -> "Empieza una racha hoy"
                        1 -> "1 día seguido"
                        else -> "${streak.currentStreak} días seguidos"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (streak.currentStreak <= 0) {
                        "Completa al menos una misión cada día para mantenerla."
                    } else {
                        "Récord: ${streak.longestStreak} · Días activos: ${streak.totalActiveDays}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Achievement row: unlocked ones show their reward, locked ones their progress. */
@Composable
fun AchievementCard(item: AchievementProgress, modifier: Modifier = Modifier) {
    val definition = item.achievement.definition
    val unlocked = item.achievement.isUnlocked
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append(definition.name)
                    append(if (unlocked) ", desbloqueado" else ", bloqueado")
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        IconBadge(
            icon = if (unlocked) definition.icon.icon else Icons.Filled.Lock,
            containerColor = if (unlocked) colors.primaryContainer else colors.surfaceContainerHigh,
            contentColor = if (unlocked) colors.onPrimaryContainer else colors.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            Text(
                text = definition.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
            Text(
                text = definition.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (unlocked) {
                Text(
                    text = "Desbloqueado · +${definition.rewardXp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary,
                )
            } else {
                ProgressBar(
                    progress = item.progress,
                    label = "Progreso de ${definition.name}: ${(item.progress * 100).toInt()} %",
                )
            }
        }
    }
}

/** Speech bubble used by the pet across the app. */
@Composable
fun PetSpeechBubble(
    text: String,
    petName: String,
    modifier: Modifier = Modifier,
) {
    MisionCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(Dimens.SpaceMd),
    ) {
        Text(
            text = petName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Labeled 0..100 stat (happiness, energy...). */
@Composable
fun LabeledStatBar(
    icon: ImageVector,
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(Dimens.IconMd))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$value %",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ProgressBar(progress = value / 100f, color = color, label = "$label: $value %")
    }
}
