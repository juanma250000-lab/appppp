package com.mision.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens

enum class GlassButtonStyle { PRIMARY, TONAL, OUTLINE, DESTRUCTIVE }

/**
 * Main action button. Primary uses the brand gradient; the rest sit on glass
 * so the hierarchy stays clear without adding more transparency.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: GlassButtonStyle = GlassButtonStyle.PRIMARY,
    leadingIcon: ImageVector? = null,
    height: Dp = Dimens.ButtonHeight,
    shape: Shape = RoundedCornerShape(Dimens.RadiusPill),
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimens.Space2xl),
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.45f,
        animationSpec = tween(Dimens.AnimFast),
        label = "buttonAlpha",
    )
    val background: Brush = when (style) {
        GlassButtonStyle.PRIMARY -> Brush.horizontalGradient(AppGradients.primary)
        GlassButtonStyle.DESTRUCTIVE -> Brush.horizontalGradient(
            listOf(Color(0xFFE5484D), Color(0xFFF06A6E)),
        )
        GlassButtonStyle.TONAL -> glassFillBrush()
        GlassButtonStyle.OUTLINE -> Brush.horizontalGradient(
            listOf(Color.Transparent, Color.Transparent),
        )
    }
    val contentColor = when (style) {
        GlassButtonStyle.PRIMARY, GlassButtonStyle.DESTRUCTIVE -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .height(height)
            .scale(alpha)
            .clip(shape)
            .background(background, shape)
            .then(
                if (style == GlassButtonStyle.OUTLINE) {
                    Modifier.border(Dimens.GlassBorder, glassBorderBrush(), shape)
                } else {
                    Modifier.border(Dimens.GlassBorder, glassBorderBrush(), shape)
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(Dimens.IconMd),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(Dimens.SpaceSm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Square icon button floating on glass (top bars, close actions...). */
@Composable
fun GlassIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.TouchTargetMin,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val shape = RoundedCornerShape(Dimens.RadiusSm)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(glassFillBrush(), shape)
            .border(Dimens.GlassBorder, glassBorderBrush(), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Dimens.SpaceMd),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(Dimens.IconLg),
        )
    }
}

/** Round small badge (counters, coins, XP pills). */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    background: Brush = Brush.horizontalGradient(AppGradients.primary),
    contentColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(Dimens.RadiusPill),
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceXs),
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(background, shape)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/**
 * Selectable chip used for mission filters and category selectors. Selected
 * state is signalled by color *and* a border, never by color alone.
 */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingEmoji: String? = null,
    height: Dp = Dimens.ButtonHeightCompact,
) {
    val shape = RoundedCornerShape(Dimens.RadiusPill)
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkSurface()) 0.55f else 0.75f),
        animationSpec = tween(Dimens.AnimFast),
        label = "chipBackground",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(Dimens.AnimFast),
        label = "chipText",
    )
    Row(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(background, shape)
            .border(
                width = if (selected) 0.dp else Dimens.GlassBorder,
                brush = glassBorderBrush(),
                shape = shape,
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = Dimens.SpaceLg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingEmoji != null) {
            Text(text = leadingEmoji, style = MaterialTheme.typography.labelMedium)
            androidx.compose.foundation.layout.Spacer(Modifier.size(Dimens.SpaceXs))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1,
        )
    }
}

/** Thin divider that fades at both ends, like a light refraction. */
@Composable
fun GlassDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight(1f)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

/** Small circular indicator dot (used by the onboarding pager). */
@Composable
fun PagerDot(active: Boolean, modifier: Modifier = Modifier) {
    val size by animateFloatAsState(
        targetValue = if (active) 10.dp.value else 7.dp.value,
        animationSpec = tween(Dimens.AnimMedium),
        label = "dotSize",
    )
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f),
                CircleShape,
            ),
    )
}

/** Interaction source helper so ripple is suppressed on custom surfaces. */
@Composable
fun rememberNoRipple(): MutableInteractionSource = remember { MutableInteractionSource() }
