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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import kotlin.math.sin

/**
 * Full screen backdrop behind every screen: a soft gradient plus slow drifting
 * colour blooms. It gives the glass surfaces something to refract without
 * competing with the content.
 */
@Composable
fun MisionBackdrop(modifier: Modifier = Modifier, animate: Boolean = true) {
    val dark = isDarkSurface()
    val colors = if (dark) AppGradients.backdropDark else AppGradients.backdropLight
    val alpha = if (dark) 0.30f else 0.42f
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val transition = rememberInfiniteTransition(label = "backdrop")
        val drift by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 14_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "backdropDrift",
        )
        val sway = if (animate) drift else 0.5f

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))

        Bloom(
            color = primary.copy(alpha = alpha),
            size = maxWidth * 0.95f,
            xOff = -maxWidth * 0.25f + maxWidth * 0.06f * sway,
            yOff = -maxHeight * 0.18f,
        )
        Bloom(
            color = secondary.copy(alpha = alpha * 0.8f),
            size = maxWidth * 0.85f,
            xOff = maxWidth * 0.50f,
            yOff = maxHeight * 0.42f + maxHeight * 0.05f * sin(sway * 6.28f),
        )
        Bloom(
            color = tertiary.copy(alpha = alpha * 0.7f),
            size = maxWidth * 0.75f,
            xOff = maxWidth * 0.30f,
            yOff = maxHeight * 0.74f,
        )
    }
}

/** Soft radial blob, blurred so it reads as light rather than a shape. */
@Composable
private fun Bloom(color: Color, size: Dp, xOff: Dp, yOff: Dp) {
    Box(
        Modifier
            .offset(x = xOff, y = yOff)
            .size(size)
            .blur(Dimens.GlassBlurStrong)
            .background(Brush.radialGradient(listOf(color, Color.Transparent))),
    )
}
