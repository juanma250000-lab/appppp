package com.mision.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Falling confetti used for level-ups, milestones and perfect days.
 *
 * Particles come from a fixed seed, so recomposition never changes the
 * pattern and nothing is allocated per frame.
 */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier, particleCount: Int = 46) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confettiProgress",
    )
    val palette = AppGradients.celebrate

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(particleCount) { index ->
                val random = Random(index * 31 + 7)
                val x = random.nextFloat() * size.width
                val speed = 0.35f + random.nextFloat() * 0.65f
                val phase = random.nextFloat()
                val spin = random.nextFloat() * 360f
                val color = palette[index % palette.size]
                val lifecycle = (progress + phase) % 1f
                val y = lifecycle * size.height * speed
                val alpha = (1f - lifecycle).coerceIn(0f, 1f)
                val wobble = sin((lifecycle + phase) * 6.283f) * 18f
                val piece = size.minDimension * 0.030f
                val squeeze = abs(cos((spin + lifecycle * 720f) * 0.0174f))
                drawRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(x + wobble, y),
                    size = Size(piece * (0.35f + squeeze), piece * 1.7f),
                )
            }
        }
    }
}

/**
 * Full-screen glass dialog used for celebrations (level up, streak milestone,
 * new achievements, perfect day).
 */
@Composable
fun CelebrationDialog(
    emoji: String,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmLabel: String = "¡Genial!",
    extraContent: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ConfettiOverlay()
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Space2xl),
                contentPadding = PaddingValues(Dimens.Space2xl),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    extraContent?.invoke()
                    GlassButton(
                        text = confirmLabel,
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Standard confirmation dialog (reset progress, delete mission...). */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Cancelar",
    destructive: Boolean = false,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space2xl),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    GlassButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        style = if (destructive) GlassButtonStyle.DESTRUCTIVE
                        else GlassButtonStyle.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassButton(
                        text = dismissLabel,
                        onClick = onDismiss,
                        style = GlassButtonStyle.TONAL,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Small blocking overlay shown while a long action runs. */
@Composable
fun LoadingOverlay(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GlassCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                Text(text = "⏳", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
