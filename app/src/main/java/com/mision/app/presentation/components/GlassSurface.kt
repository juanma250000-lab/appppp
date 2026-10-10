package com.mision.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
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
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    // The outer box takes the caller's size; its children get it as a minimum
    // so the shadow layer and the surface always match exactly.
    Box(modifier = modifier, propagateMinConstraints = true) {
        // Android renders an elevation shadow under the whole surface, and a
        // translucent fill let it show through as a hard-edged inner
        // rectangle. The shadow lives on its own layer, clipped so it is only
        // ever drawn outside the shape.
        Spacer(
            Modifier
                .matchParentSize()
                .drawWithCache {
                    val outline = Path().apply {
                        addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache))
                    }
                    onDrawWithContent {
                        clipPath(outline, ClipOp.Difference) {
                            this@onDrawWithContent.drawContent()
                        }
                    }
                }
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = false,
                    ambientColor = GlassShadowAmbient,
                    spotColor = GlassShadowSpot,
                ),
        )
        Box(
            modifier = Modifier
                .background(fillBrush, shape)
                .border(Dimens.GlassBorder, borderBrush, shape)
                .clip(shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(contentPadding),
            content = content,
        )
    }
}

/** Convenience card variant with the standard screen padding. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusMd),
    elevation: Dp = Dimens.GlassElevation,
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable BoxScope.() -> Unit,
) = GlassSurface(
    modifier = modifier,
    shape = shape,
    elevation = elevation,
    contentPadding = contentPadding,
    onClick = onClick,
    onClickLabel = onClickLabel,
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

/**
 * Panel for dialogs. A dialog floats over busy content, so it uses a nearly
 * opaque fill (still with the glass border and highlight) to keep its text
 * readable, and a capped width so it does not stretch on tablets.
 */
@Composable
fun GlassDialogSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    content: @Composable BoxScope.() -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    GlassSurface(
        modifier = modifier.widthIn(max = Dimens.DialogMaxWidth),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg),
        elevation = Dimens.GlassElevationHigh,
        fillBrush = Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
                surface.copy(alpha = 0.97f),
            ),
        ),
        contentPadding = contentPadding,
        content = content,
    )
}

private val GlassShadowAmbient = Color.Black.copy(alpha = 0.10f)
private val GlassShadowSpot = Color.Black.copy(alpha = 0.18f)

/** Parses "#RRGGBB" swatches used by the shop catalogue. */
fun parseHexColor(hex: String, fallback: Color): Color = runCatching {
    val clean = hex.removePrefix("#")
    Color("#$clean".toColorInt())
}.getOrDefault(fallback)
