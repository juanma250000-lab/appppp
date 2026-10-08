package com.mision.app.presentation.components

import com.mision.app.core.text.plural
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.mision.app.domain.model.Achievement
import com.mision.app.domain.model.AchievementIcon
import com.mision.app.domain.model.StreakState
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.StreakFlame

/** Row title with an optional action on the right. */
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.RadiusXs))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
            )
        }
    }
}

/** Friendly empty placeholder with an action to recover from it. */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.displayMedium)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            if (actionLabel != null && onAction != null) {
                GlassButton(
                    text = actionLabel,
                    onClick = onAction,
                    style = GlassButtonStyle.TONAL,
                    height = Dimens.ButtonHeightCompact,
                )
            }
        }
    }
}

/** Compact metric tile (misiones, días, nivel...). */
@Composable
fun StatTile(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    gradient: List<Color> = AppGradients.primary,
) {
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
            Box(
                modifier = Modifier
                    .size(Dimens.IconXl)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
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
}

/** Hero card for the streak, with the flame accent and the longest record. */
@Composable
fun StreakCard(streak: StreakState, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.IconXxl)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(AppGradients.streak)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🔥", style = MaterialTheme.typography.titleLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                Text(
                    text = if (streak.isActive || streak.currentStreak > 0) {
                        plural(streak.currentStreak, "día seguido", "días seguidos")
                    } else {
                        "Empieza una racha hoy"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        streak.currentStreak <= 0 ->
                            "Completa al menos una misión cada día para mantenerla."
                        else -> "Récord: ${plural(streak.longestStreak, "día", "días")} · " +
                            "Total: ${plural(streak.totalActiveDays, "día", "días")}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Maps the catalogue icon to a Material icon. */
fun AchievementIcon.toImageVector(): ImageVector = when (this) {
    AchievementIcon.TROPHY -> Icons.Filled.EmojiEvents
    AchievementIcon.FLAME -> Icons.Filled.LocalFireDepartment
    AchievementIcon.STAR -> Icons.Filled.Star
    AchievementIcon.CHECK -> Icons.Filled.CheckCircle
    AchievementIcon.BOOK -> Icons.AutoMirrored.Filled.MenuBook
    AchievementIcon.HEART -> Icons.Filled.Favorite
    AchievementIcon.BOLT -> Icons.Filled.Bolt
    AchievementIcon.MEDAL -> Icons.Filled.WorkspacePremium
    AchievementIcon.CROWN -> Icons.Filled.EmojiEvents
    AchievementIcon.SPARKLE -> Icons.Filled.AutoAwesome
}

/** Achievement card: unlocked ones glow, locked ones show their progress. */
@Composable
fun AchievementCard(
    achievement: Achievement,
    currentValue: Int,
    target: Int,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(Dimens.TouchTargetMin)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(
                        if (achievement.isUnlocked) {
                            Brush.linearGradient(AppGradients.celebrate)
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            )
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = achievement.definition.icon.toImageVector(),
                    contentDescription = null,
                    tint = if (achievement.isUnlocked) Color(0xFF2A2440)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconLg),
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = Dimens.SpaceMd)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                Text(
                    text = achievement.definition.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = achievement.definition.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (achievement.isUnlocked) {
                    Text(
                        text = "Desbloqueado · +${achievement.definition.rewardXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = StreakFlame,
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    GradientProgressBar(
                        progress = if (target <= 0) 0f else currentValue.toFloat() / target,
                        modifier = Modifier.fillMaxWidth(),
                        label = "Progreso de ${achievement.definition.name}",
                    )
                }
            }
        }
    }
}

/** Speech bubble used by the pet across the app. */
@Composable
fun PetSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    petName: String? = null,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Text(text = "💬", style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(start = Dimens.SpaceSm)) {
                if (petName != null) {
                    Text(
                        text = petName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Labeled stat bar (happiness, energy...). */
@Composable
fun LabeledStatBar(
    emoji: String,
    label: String,
    value: Int,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$value %",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GradientProgressBar(
            progress = value / 100f,
            colors = colors,
            modifier = Modifier.fillMaxWidth(),
            label = label,
        )
    }
}

