package com.mision.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import com.mision.app.presentation.LocalAnimationsEnabled
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import kotlin.math.sin

/**
 * Full screen backdrop behind every screen: a soft gradient plus slow drifting
 * colour blooms. It gives the glass surfaces something to refract without
 * competing with the content.
 */
@Composable
fun MisionBackdrop(
    modifier: Modifier = Modifier,
    animate: Boolean = LocalAnimationsEnabled.current,
) {
    val dark = isDarkSurface()
    val colors = if (dark) AppGradients.backdropDark else AppGradients.backdropLight
    val alpha = if (dark) 0.30f else 0.42f
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    // The drift is only read inside the offset lambdas (layout phase), so the
    // backdrop never recomposes while it moves.
    val sway: State<Float> = if (animate) {
        rememberInfiniteTransition(label = "backdrop").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 14_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "backdropDrift",
        )
    } else {
        remember { mutableFloatStateOf(0.5f) }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))

        Bloom(
            color = primary.copy(alpha = alpha),
            size = width * 0.95f,
            offset = { DpOffset(-width * 0.25f + width * 0.06f * sway.value, -height * 0.18f) },
        )
        Bloom(
            color = secondary.copy(alpha = alpha * 0.8f),
            size = width * 0.85f,
            offset = {
                DpOffset(width * 0.50f, height * 0.42f + height * 0.05f * sin(sway.value * 6.28f))
            },
        )
        Bloom(
            color = tertiary.copy(alpha = alpha * 0.7f),
            size = width * 0.75f,
            offset = { DpOffset(width * 0.30f, height * 0.74f) },
        )
    }
}

/** Soft radial blob, blurred so it reads as light rather than a shape. */
@Composable
private fun Bloom(color: Color, size: Dp, offset: () -> DpOffset) {
    Box(
        Modifier
            .offset {
                val value = offset()
                IntOffset(value.x.roundToPx(), value.y.roundToPx())
            }
            .size(size)
            .blur(Dimens.GlassBlurStrong)
            .background(Brush.radialGradient(listOf(color, Color.Transparent))),
    )
}
