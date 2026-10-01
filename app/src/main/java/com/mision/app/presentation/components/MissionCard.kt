package com.mision.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mision.app.domain.model.Mission
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors

/**
 * The mission row: completion control, title, category and the rewards at a
 * glance. The circle toggles completion; tapping the body runs [onClick]
 * (only when the caller provides one, so nothing looks tappable for nothing).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissionCard(
    mission: Mission,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val game = MisionColors.game
    MisionCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            CompletionToggle(
                completed = mission.isCompleted,
                missionTitle = mission.title,
                onToggle = onToggle,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (mission.isCompleted) colors.onSurfaceVariant else colors.onSurface,
                    textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (mission.description.isNotBlank() && !mission.isCompleted) {
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                    modifier = Modifier.padding(top = Dimens.SpaceXs),
                ) {
                    InfoChip(text = mission.category.displayName, icon = mission.category.icon)
                    InfoChip(
                        text = "+${mission.xpReward} XP",
                        icon = Icons.Filled.Star,
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    )
                    InfoChip(
                        text = mission.coinReward.toString(),
                        icon = MisionIcons.Coin,
                        containerColor = game.coin.copy(alpha = COIN_CHIP_ALPHA),
                        contentColor = colors.onSurface,
                    )
                    mission.durationMinutes?.let { minutes ->
                        InfoChip(text = "$minutes min", icon = Icons.Filled.Schedule)
                    }
                }
            }
        }
    }
}

/** Circular check that doubles as the completion control (48dp target). */
@Composable
private fun CompletionToggle(
    completed: Boolean,
    missionTitle: String,
    onToggle: () -> Unit,
) {
    val game = MisionColors.game
    val outline = MaterialTheme.colorScheme.outline
    val fill by animateColorAsState(
        targetValue = if (completed) game.success else Color.Transparent,
        animationSpec = tween(Dimens.AnimMedium),
        label = "completionFill",
    )
    Box(
        modifier = Modifier
            .size(Dimens.TouchTargetMin)
            .clip(CircleShape)
            .toggleable(value = completed, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics {
                contentDescription = if (completed) "Desmarcar $missionTitle" else "Completar $missionTitle"
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.IconContainer - Dimens.SpaceXs)
                .clip(CircleShape)
                .background(fill)
                .border(Dimens.Hairline * 2, if (completed) game.success else outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = game.onSuccess,
                    modifier = Modifier.size(Dimens.IconMd),
                )
            }
        }
    }
}

private const val COIN_CHIP_ALPHA = 0.35f
