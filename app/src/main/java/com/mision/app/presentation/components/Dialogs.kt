package com.mision.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.mision.app.presentation.celebrations.CelebrationUi
import com.mision.app.presentation.celebrations.icon
import com.mision.app.presentation.theme.ConfettiColors
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.LocalAnimationsEnabled
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A single burst of falling confetti. It plays once (no endless loop) and
 * nothing is allocated per frame: particles come from a fixed seed.
 */
@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(Dimens.AnimConfetti, easing = LinearEasing))
    }
    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        val piece = size.minDimension * 0.028f
        repeat(PARTICLES) { index ->
            val random = Random(index * 31 + 7)
            val speed = 0.55f + random.nextFloat() * 0.45f
            val delay = random.nextFloat() * 0.25f
            val life = ((t - delay) / (1f - delay)).coerceIn(0f, 1f)
            if (life <= 0f || life >= 1f) return@repeat
            val x = random.nextFloat() * size.width + sin(life * 9f + index) * piece * 2f
            val y = life * size.height * speed
            val squeeze = abs(cos(life * 12f + index))
            drawRect(
                color = ConfettiColors[index % ConfettiColors.size].copy(alpha = 1f - life),
                topLeft = Offset(x, y),
                size = Size(piece * (0.4f + squeeze), piece * 1.6f),
            )
        }
    }
}

/** Celebration (level up, streak milestone, achievements, perfect day). */
@Composable
fun CelebrationDialog(
    celebration: CelebrationUi,
    onDismiss: () -> Unit,
    confirmLabel: String = "¡Genial!",
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(contentAlignment = Alignment.Center) {
            if (LocalAnimationsEnabled.current) ConfettiBurst()
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.widthIn(max = Dimens.ContentMaxWidth),
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.Space2xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    IconBadge(icon = celebration.kind.icon, size = Dimens.BrandMarkLarge)
                    Text(
                        text = celebration.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = celebration.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    celebration.details.forEach { detail ->
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    MisionButton(
                        text = confirmLabel,
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpaceSm),
                    )
                }
            }
        }
    }
}

/** Standard confirmation for destructive or spending actions. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Cancelar",
    destructive: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                content?.invoke()
                Text(message)
            }
        },
        confirmButton = {
            MisionButton(
                text = confirmLabel,
                onClick = onConfirm,
                style = if (destructive) MisionButtonStyle.DESTRUCTIVE else MisionButtonStyle.PRIMARY,
                height = Dimens.ButtonHeightCompact,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
    )
}

/** Full screen loading state with the logo, used while the app decides where to start. */
@Composable
fun LoadingState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg, Alignment.CenterVertically),
    ) {
        BrandMark(size = Dimens.BrandMarkLarge)
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val PARTICLES = 44
