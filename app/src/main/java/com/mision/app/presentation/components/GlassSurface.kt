package com.mision.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.toColorInt
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.GlassTokens

/**
 * Detects the *effective* theme (which honours the in-app setting, unlike
 * [isSystemInDarkTheme]) so glass surfaces look correct in both modes.
 */
@Composable
fun isDarkSurface(): Boolean =
    MaterialTheme.colorScheme.onSurface.luminance() > 0.5f

/** Translucent fill of a glass surface for the current theme. */
@Composable
fun glassFillBrush(): Brush {
    val dark = isDarkSurface()
    val top = if (dark) Color.White.copy(alpha = GlassTokens.DarkFillAlpha * 0.45f)
    else Color.White.copy(alpha = GlassTokens.LightFillAlpha)
    val bottom = if (dark) MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    return Brush.verticalGradient(listOf(top, bottom))
}

/** Hairline border: bright at the top, fading towards the bottom. */
@Composable
fun glassBorderBrush(): Brush {
    val highlight = if (isDarkSurface()) AppGradients.glassHighlightDark
    else AppGradients.glassHighlight
    return Brush.verticalGradient(highlight)
}

/**
 * The building block of the whole interface: a translucent panel with a soft
 * highlight, a hairline glass border and a gentle elevation.
 *
 * Opacity is deliberately conservative ([GlassTokens]) so text stays readable
 * in both light and dark mode instead of turning into a foggy overlay.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusMd),
    elevation: Dp = Dimens.GlassElevation,
    fillBrush: Brush = glassFillBrush(),
    borderBrush: Brush = glassBorderBrush(),
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false)
            .background(fillBrush, shape)
            .border(Dimens.GlassBorder, borderBrush, shape)
            .clip(shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Convenience card variant with the standard screen padding. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusMd),
    elevation: Dp = Dimens.GlassElevation,
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    content: @Composable BoxScope.() -> Unit,
) = GlassSurface(
    modifier = modifier,
    shape = shape,
    elevation = elevation,
    contentPadding = contentPadding,
    content = content,
)

/**
 * Same as [GlassSurface] but with a stronger tint (used for hero panels such
 * as the pet stage), keeping contrast without extra transparency.
 */
@Composable
fun TintedGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg),
    tint: Color = MaterialTheme.colorScheme.primary,
    tintAlpha: Float = 0.16f,
    elevation: Dp = Dimens.GlassElevationHigh,
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    content: @Composable BoxScope.() -> Unit,
) {
    val brush = Brush.verticalGradient(
        listOf(
            tint.copy(alpha = tintAlpha + 0.10f),
            tint.copy(alpha = tintAlpha * 0.45f),
        ),
    )
    GlassSurface(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        fillBrush = brush,
        contentPadding = contentPadding,
        content = content,
    )
}

/** Parses "#RRGGBB" swatches used by the shop catalogue. */
fun parseHexColor(hex: String, fallback: Color): Color = runCatching {
    val clean = hex.removePrefix("#")
    Color("#$clean".toColorInt())
}.getOrDefault(fallback)
