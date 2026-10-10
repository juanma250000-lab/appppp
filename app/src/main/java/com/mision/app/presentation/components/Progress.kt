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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.mision.app.core.text.plural
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.CoinGold
import com.mision.app.presentation.theme.Dimens

/**
 * Animated gradient progress bar. The track stays solid (never a hairline) so
 * the value is perceivable for users with low vision, and the semantic
 * description carries the exact percentage for TalkBack.
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = Dimens.ProgressBarHeight,
    colors: List<Color> = AppGradients.primary,
    shape: Shape = RoundedCornerShape(Dimens.RadiusPill),
    label: String? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(Dimens.AnimMedium),
        label = "progress",
    )
    val percent = (animated * 100).toInt()
    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f), shape)
            .semantics {
                contentDescription = label?.let { "$it: $percent por ciento" }
                    ?: "$percent por ciento completado"
            },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .background(Brush.horizontalGradient(colors), shape),
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
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(Dimens.IconXxl)
                .clip(RoundedCornerShape(Dimens.RadiusSm))
                .background(Brush.linearGradient(AppGradients.xp), RoundedCornerShape(Dimens.RadiusSm)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF10231A),
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier
                .padding(start = Dimens.SpaceMd)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Nivel $level",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    // xpToNext is what is still missing, so the level span is into + missing.
                    text = if (xpToNext <= 0) "Nivel máximo" else "$xpIntoLevel / ${xpIntoLevel + xpToNext} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GradientProgressBar(
                progress = progress,
                colors = AppGradients.xp,
                modifier = Modifier.fillMaxWidth(),
                label = "Experiencia del nivel $level",
            )
        }
    }
}

/** Coin counter pill used in top bars and the shop. */
@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier) {
    GlassPill(
        // Read as "115 monedas", not just a bare number.
        modifier = modifier.clearAndSetSemantics {
            contentDescription = plural(coins, "moneda", "monedas")
        },
        background = Brush.horizontalGradient(AppGradients.coin),
        contentColor = Color(0xFF3A2A00),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFF3A2A00),
            modifier = Modifier.size(Dimens.IconSm),
        )
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF3A2A00),
            modifier = Modifier.padding(start = Dimens.SpaceXs),
        )
    }
}

/** Reward chip: "+25 XP" or "+10" with an icon. */
@Composable
fun RewardChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(tint.copy(alpha = 0.14f), RoundedCornerShape(Dimens.RadiusPill))
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimens.IconSm),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Coin-styled reward chip (uses the gold accent instead of an icon tint). */
@Composable
fun CoinChip(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(CoinGold.copy(alpha = 0.18f), RoundedCornerShape(Dimens.RadiusPill))
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Text(text = "🪙", style = MaterialTheme.typography.labelSmall)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
