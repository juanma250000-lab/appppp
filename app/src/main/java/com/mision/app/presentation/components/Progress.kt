package com.mision.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors

/**
 * Animated linear progress. The track is always visible so the value is
 * perceivable with low vision, and TalkBack reads it as a real progress bar.
 */
@Composable
fun ProgressBar(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = Dimens.ProgressBarHeight,
) {
    val value = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(Dimens.AnimMedium),
        label = "progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
            },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(color),
        )
    }
}

/** Level badge + XP progress, the main progression indicator. */
@Composable
fun XpIndicator(
    level: Int,
    progress: Float,
    xpIntoLevel: Int,
    xpToNext: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.IconContainer)
                .clip(MaterialTheme.shapes.small)
                .background(colors.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Nivel $level",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface,
                )
                Text(
                    text = if (xpToNext <= 0) "Nivel máximo" else "$xpIntoLevel / ${xpIntoLevel + xpToNext} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            ProgressBar(
                progress = progress,
                color = colors.secondary,
                label = "Experiencia del nivel $level",
            )
        }
    }
}

/** Coin balance pill: gold, with the coin glyph so it never relies on colour. */
@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier) {
    val game = MisionColors.game
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(game.coin)
            .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs)
            .semantics(mergeDescendants = true) { contentDescription = "$coins monedas" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Icon(
            imageVector = MisionIcons.Coin,
            contentDescription = null,
            tint = game.onCoin,
            modifier = Modifier.size(Dimens.IconSm),
        )
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = game.onCoin,
        )
    }
}

/** Small reward/metadata chip: icon + short text ("+25 XP", "10", "30 min"). */
@Composable
fun InfoChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(Dimens.IconSm),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}
