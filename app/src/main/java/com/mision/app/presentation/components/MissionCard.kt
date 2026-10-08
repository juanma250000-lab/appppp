package com.mision.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mision.app.core.time.SpanishLocale
import com.mision.app.domain.model.Mission
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.SuccessGreen

/**
 * The mission row: glass card, animated completion check, category context and
 * the rewards at a glance. Tapping the circle completes the mission, tapping
 * the card body opens its details.
 */
@Composable
fun MissionCard(
    mission: Mission,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val statusLabel = if (mission.isCompleted) "completada" else "pendiente"
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "Misión ${mission.title}, $statusLabel, ${mission.category.displayName}"
            },
        contentPadding = PaddingValues(Dimens.SpaceMd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompletionCircle(completed = mission.isCompleted, onClick = onToggle)

            Column(
                modifier = Modifier
                    .padding(start = Dimens.SpaceMd)
                    .fillMaxWidth()
                    .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
            ) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (mission.description.isNotBlank()) {
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Dimens.SpaceXs),
                    )
                }
                AnimatedVisibility(
                    visible = !mission.isCompleted,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Row(
                        modifier = Modifier.padding(top = Dimens.SpaceSm),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RewardChip(
                            text = "+${mission.xpReward} XP",
                            icon = Icons.Filled.Check,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        CoinChip(text = mission.coinReward.toString())
                        if (mission.reminderEnabled) {
                            val time = String.format(
                                SpanishLocale,
                                "%02d:%02d",
                                mission.reminderHour,
                                mission.reminderMinute,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.semantics(mergeDescendants = true) {
                                    contentDescription = "Recordatorio a las $time"
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(Dimens.IconSm),
                                )
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Circular, animated check that doubles as the completion control. */
@Composable
fun CompletionCircle(completed: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = if (completed) 1f else 0f,
        animationSpec = tween(Dimens.AnimMedium),
        label = "checkProgress",
    )
    val contentColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .size(Dimens.TouchTargetMin)
            .clip(CircleShape)
            .background(
                if (completed) Brush.linearGradient(AppGradients.celebrate)
                else Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
                CircleShape,
            )
            .border(
                width = 2.dp,
                color = if (completed) SuccessGreen else contentColor.copy(alpha = 0.35f),
                shape = CircleShape,
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics {
                contentDescription = if (completed) "Desmarcar misión" else "Completar misión"
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = if (completed) Color(0xFF10231A) else Color.Transparent,
            modifier = Modifier
                .size(Dimens.IconLg)
                .scale(progress),
        )
    }
}

