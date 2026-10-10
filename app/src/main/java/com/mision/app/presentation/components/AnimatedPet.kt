package com.mision.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mision.app.domain.model.Pet
import com.mision.app.presentation.LocalAnimationsEnabled
import com.mision.app.presentation.theme.Dimens
import kotlinx.coroutines.launch

/**
 * The mascot, Nube, drawn by [drawCloudMascot] so it stays crisp at any size
 * and can wear every cosmetic from the shop.
 *
 * Idle motion (floating, breathing, a slight turn and blinking) follows the
 * "Animaciones" setting and the system "remove animations" preference through
 * [LocalAnimationsEnabled]; callers can still force it off with [animate].
 * Without motion the cloud is drawn in its neutral pose, never hidden.
 *
 * @param interactive tapping the cloud makes it hop (only where that is the
 * whole point, e.g. the pet stage).
 * @param reactionKey every new non-null value plays the same hop, so screens
 * can celebrate a successful action.
 */
@Composable
fun AnimatedPet(
    pet: Pet,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    animate: Boolean = LocalAnimationsEnabled.current,
    accessibilityLabel: String? = null,
    interactive: Boolean = false,
    reactionKey: Any? = null,
) {
    val label = accessibilityLabel
        ?: "Mascota ${pet.name}, ${pet.mood.displayName.lowercase()}"
    val palette = remember(pet.equipped, pet.mood) { mascotPaletteFor(pet.equipped, pet.mood) }
    val hop = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    fun playHop() {
        if (!animate) return
        scope.launch {
            hop.snapTo(0f)
            hop.animateTo(1f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
            hop.snapTo(0f)
        }
    }

    LaunchedEffect(reactionKey) {
        if (reactionKey != null) playHop()
    }

    var sized = modifier.size(size)
    sized = if (interactive) {
        sized.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClickLabel = "Hacer que ${pet.name} salte",
        ) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            playHop()
        }.semantics { contentDescription = label }
    } else {
        sized.semantics { contentDescription = label }
    }

    if (!animate) {
        // No infinite transition at all: a still cloud costs a single draw.
        Canvas(modifier = sized) { drawCloudMascot(pet, palette) }
        return
    }

    val transition = rememberInfiniteTransition(label = "nubeIdle")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Dimens.AnimPetIdle * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "nubePhase",
    )
    val blinkCycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "nubeBlink",
    )

    // Animated values are only read inside the draw lambda, so each frame
    // re-draws the canvas without recomposing.
    Canvas(modifier = sized) {
        drawCloudMascot(
            pet = pet,
            palette = palette,
            phase = phase,
            blink = blinkAmount(blinkCycle),
            hop = hop.value,
            moving = true,
        )
    }
}

/** Eyelid closure for a point of the blink cycle: a quick close/open near its end. */
internal fun blinkAmount(cycle: Float): Float {
    val start = 0.93f
    if (cycle < start) return 0f
    val t = (cycle - start) / (1f - start)
    return 1f - kotlin.math.abs(t * 2f - 1f)
}

/**
 * Motionless mascot that fills the space it is given. Used where many are on
 * screen at once (shop previews).
 */
@Composable
fun StaticPet(pet: Pet, modifier: Modifier = Modifier) {
    val palette = remember(pet.equipped, pet.mood) { mascotPaletteFor(pet.equipped, pet.mood) }
    Canvas(modifier = modifier) { drawCloudMascot(pet, palette) }
}
