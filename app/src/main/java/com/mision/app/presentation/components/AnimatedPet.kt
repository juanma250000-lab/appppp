package com.mision.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.toColorInt
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.LocalAnimationsEnabled
import kotlin.math.cos
import kotlin.math.sin

/**
 * The virtual pet, drawn with Compose primitives so it stays crisp at any
 * size and can wear every cosmetic of the shop.
 *
 * The idle loop (bob, blink, orbiting effects, emotes) only exists while
 * [animate] is true; otherwise a single still frame is drawn, which is also
 * what the shop uses for its product previews.
 */
@Composable
fun AnimatedPet(
    pet: Pet,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.PetStage,
    animate: Boolean = LocalAnimationsEnabled.current,
    accessibilityLabel: String? = null,
) {
    val look = remember(pet.equipped) { PetLook.of(pet.equipped) }
    val label = accessibilityLabel ?: "Mascota ${pet.name}, ${pet.mood.displayName.lowercase()}"
    val canvasModifier = modifier
        .size(size)
        .semantics { contentDescription = label }

    if (animate) {
        val transition = rememberInfiniteTransition(label = "petIdle")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = TWO_PI,
            animationSpec = infiniteRepeatable(tween(Dimens.AnimPetIdle, easing = LinearEasing), RepeatMode.Restart),
            label = "petPhase",
        )
        val blink by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(Dimens.AnimPetBlink, easing = LinearEasing), RepeatMode.Restart),
            label = "petBlink",
        )
        // State is read inside the draw lambda: frames only redraw, never recompose.
        Canvas(canvasModifier) { drawPet(pet.mood, look, phase = phase, blink = blink > BLINK_THRESHOLD, moving = true) }
    } else {
        Canvas(canvasModifier) { drawPet(pet.mood, look, phase = 0f, blink = false, moving = false) }
    }
}

/** Parses "#RRGGBB" catalogue colours, falling back when the value is invalid. */
fun parseHexColor(hex: String, fallback: Color): Color =
    runCatching { Color(hex.toColorInt()) }.getOrDefault(fallback)

/** Equipped cosmetics resolved once to drawing styles (no lookups per frame). */
@Immutable
private data class PetLook(
    val light: Color,
    val base: Color,
    val dark: Color,
    val halo: Color?,
    val hat: String?,
    val accessory: String?,
    val effect: String?,
    val emote: String?,
    val skin: String?,
) {
    companion object {
        fun of(equipped: EquippedCosmetics): PetLook {
            fun item(id: String?) = id?.let(ShopCatalog::byId)
            // Skins are the premium tier, so they win over plain colours.
            val bodyItem = item(equipped.skin) ?: item(equipped.color)
            val base = bodyItem?.let { parseHexColor(it.previewColorHex, DefaultBody) } ?: DefaultBody
            return PetLook(
                light = mix(base, Color.White, 0.35f),
                base = base,
                dark = mix(base, Shade, 0.35f),
                halo = item(equipped.background)?.let { parseHexColor(it.previewColorHex, DefaultBody) },
                hat = item(equipped.hat)?.styleId,
                accessory = item(equipped.accessory)?.styleId,
                effect = item(equipped.effect)?.styleId,
                emote = item(equipped.emote)?.styleId,
                skin = item(equipped.skin)?.styleId,
            )
        }
    }
}

private fun DrawScope.drawPet(
    mood: PetMood,
    look: PetLook,
    phase: Float,
    blink: Boolean,
    moving: Boolean,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val bodyRadius = w * 0.29f
    val bob = if (moving) sin(phase) * w * 0.018f else 0f
    val bodyCy = h * 0.55f + bob

    // Background halo ("Fondos") and ground shadow stay still.
    look.halo?.let { halo ->
        drawCircle(
            brush = Brush.radialGradient(
                listOf(halo.copy(alpha = 0.55f), halo.copy(alpha = 0f)),
                center = Offset(cx, h * 0.55f),
                radius = w * 0.5f,
            ),
            radius = w * 0.5f,
            center = Offset(cx, h * 0.55f),
        )
    }
    drawOval(
        color = Color.Black.copy(alpha = 0.14f),
        topLeft = Offset(cx - bodyRadius * 0.85f, h * 0.87f),
        size = Size(bodyRadius * 1.7f, bodyRadius * 0.26f),
    )

    // The "dance" emote sways the whole body.
    val sway = if (look.emote == "dance") {
        if (moving) sin(phase * 2f) * DANCE_TILT else DANCE_TILT
    } else {
        0f
    }
    withTransform({ rotate(sway, Offset(cx, h * 0.86f)) }) {
        drawBody(mood, look, cx, bodyCy, bodyRadius, phase, blink, moving)
    }

    look.effect?.let { drawEffect(it, cx, bodyCy, bodyRadius, phase) }
    if (look.emote == "dance") drawMusicNote(cx + bodyRadius * 1.15f, bodyCy - bodyRadius * 1.05f, bodyRadius, phase, moving)
}

private fun DrawScope.drawBody(
    mood: PetMood,
    look: PetLook,
    cx: Float,
    bodyCy: Float,
    bodyRadius: Float,
    phase: Float,
    blink: Boolean,
    moving: Boolean,
) {
    // Ears.
    val earRadius = bodyRadius * 0.42f
    val earY = bodyCy - bodyRadius * 0.78f
    listOf(-1f, 1f).forEach { side ->
        val earX = cx + side * bodyRadius * 0.72f
        drawCircle(color = look.dark, radius = earRadius, center = Offset(earX, earY))
        drawCircle(color = look.base, radius = earRadius * 0.55f, center = Offset(earX, earY + earRadius * 0.18f))
    }

    // "Saludo" emote: a paw waving next to the body.
    if (look.emote == "wave") {
        val lift = if (moving) sin(phase * 3f) * bodyRadius * 0.12f else 0f
        drawCircle(
            color = look.dark,
            radius = bodyRadius * 0.24f,
            center = Offset(cx + bodyRadius * 1.02f, bodyCy - bodyRadius * 0.35f + lift),
        )
    }

    // Body.
    val pulse = if (moving && mood == PetMood.CELEBRANDO) 1f + sin(phase * 3f) * 0.02f else 1f
    val radius = bodyRadius * pulse
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(look.light, look.base, look.dark),
            startY = bodyCy - radius,
            endY = bodyCy + radius,
        ),
        radius = radius,
        center = Offset(cx, bodyCy),
    )
    drawOval(
        color = Color.White.copy(alpha = if (look.skin == "gold") 0.38f else 0.22f),
        topLeft = Offset(cx - radius * 0.55f, bodyCy - radius * 0.78f),
        size = Size(radius * 0.75f, radius * 0.34f),
    )
    if (look.skin == "galaxy") {
        GalaxySpecks.forEach { (dx, dy) ->
            drawCircle(Color.White.copy(alpha = 0.8f), radius * 0.035f, Offset(cx + dx * radius, bodyCy + dy * radius))
        }
    }

    drawFace(mood, cx, bodyCy, radius, blink)
    look.hat?.let { drawHat(it, cx, bodyCy - radius, radius, look.base) }
    look.accessory?.let { drawAccessory(it, cx, bodyCy, radius) }
}

private fun DrawScope.drawFace(mood: PetMood, cx: Float, bodyCy: Float, radius: Float, blink: Boolean) {
    val eyeOffsetX = radius * 0.36f
    val eyeY = bodyCy - radius * 0.10f
    val eyeRadius = radius * 0.145f
    val eyeHeight = if (blink) eyeRadius * 0.16f else eyeRadius * 2f
    listOf(-1f, 1f).forEach { side ->
        drawOval(
            color = Ink,
            topLeft = Offset(cx + side * eyeOffsetX - eyeRadius, eyeY - eyeHeight / 2f),
            size = Size(eyeRadius * 2f, eyeHeight),
        )
        if (!blink) {
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.32f,
                center = Offset(cx + side * eyeOffsetX + eyeRadius * 0.3f, eyeY - eyeRadius * 0.35f),
            )
        }
        drawCircle(
            color = Cheek,
            radius = radius * 0.12f,
            center = Offset(cx + side * radius * 0.55f, eyeY + radius * 0.30f),
        )
    }

    val mouthWidth = radius * 0.52f
    val mouthHeight = radius * 0.34f
    val mouthTop = eyeY + radius * 0.34f
    val stroke = Stroke(width = radius * 0.07f, cap = StrokeCap.Round)
    val mouthTopLeft = Offset(cx - mouthWidth / 2f, mouthTop - mouthHeight / 2f)
    when (mood) {
        PetMood.CELEBRANDO -> drawOval(
            color = Ink,
            topLeft = Offset(cx - mouthWidth / 2f, mouthTop),
            size = Size(mouthWidth, mouthHeight),
        )
        PetMood.TRISTE, PetMood.CANSADO ->
            drawArc(Ink, 200f, 140f, false, mouthTopLeft, Size(mouthWidth, mouthHeight), style = stroke)
        else -> drawArc(Ink, 20f, 140f, false, mouthTopLeft, Size(mouthWidth, mouthHeight), style = stroke)
    }
}

private fun DrawScope.drawHat(styleId: String, cx: Float, top: Float, radius: Float, bodyColor: Color) {
    when (styleId) {
        "party" -> {
            val cone = Path().apply {
                moveTo(cx - radius * 0.42f, top + radius * 0.16f)
                lineTo(cx, top - radius * 0.72f)
                lineTo(cx + radius * 0.42f, top + radius * 0.16f)
                close()
            }
            drawPath(cone, color = Color(0xFFFF6F91))
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(cx - radius * 0.2f, top - radius * 0.2f),
                end = Offset(cx + radius * 0.27f, top - radius * 0.05f),
                strokeWidth = radius * 0.06f,
            )
            drawCircle(color = Color(0xFFFFD166), radius = radius * 0.13f, center = Offset(cx, top - radius * 0.76f))
        }
        "astro" -> {
            drawArc(
                color = Color(0xFF3D2FB8),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - radius * 0.58f, top - radius * 0.34f),
                size = Size(radius * 1.16f, radius * 0.8f),
            )
            drawCircle(Color(0xFFFFE27A), radius * 0.07f, Offset(cx - radius * 0.18f, top - radius * 0.12f))
            drawCircle(Color(0xFFFFE27A), radius * 0.05f, Offset(cx + radius * 0.22f, top - radius * 0.2f))
        }
        "crown" -> {
            val crown = Path().apply {
                moveTo(cx - radius * 0.5f, top + radius * 0.14f)
                lineTo(cx - radius * 0.5f, top - radius * 0.4f)
                lineTo(cx - radius * 0.24f, top - radius * 0.12f)
                lineTo(cx, top - radius * 0.5f)
                lineTo(cx + radius * 0.24f, top - radius * 0.12f)
                lineTo(cx + radius * 0.5f, top - radius * 0.4f)
                lineTo(cx + radius * 0.5f, top + radius * 0.14f)
                close()
            }
            drawPath(crown, color = Color(0xFFF5C542))
            drawPath(crown, color = Color(0xFFB8860B), style = Stroke(width = radius * 0.04f))
        }
        else -> drawRect(
            color = bodyColor,
            topLeft = Offset(cx - radius * 0.5f, top - radius * 0.05f),
            size = Size(radius, radius * 0.25f),
        )
    }
}

private fun DrawScope.drawAccessory(styleId: String, cx: Float, bodyCy: Float, radius: Float) {
    val eyeY = bodyCy - radius * 0.10f
    when (styleId) {
        "glasses" -> {
            val lensRadius = radius * 0.24f
            val offset = radius * 0.36f
            val frame = Color(0xFF2B2D42)
            listOf(-1f, 1f).forEach { side ->
                val center = Offset(cx + side * offset, eyeY)
                drawCircle(Color(0xFF2B2D42).copy(alpha = 0.55f), lensRadius, center)
                drawCircle(frame, lensRadius, center, style = Stroke(width = radius * 0.06f))
            }
            drawLine(
                color = frame,
                start = Offset(cx - offset + lensRadius, eyeY),
                end = Offset(cx + offset - lensRadius, eyeY),
                strokeWidth = radius * 0.06f,
            )
        }
        "scarf" -> {
            drawOval(
                color = Color(0xFFE5477E),
                topLeft = Offset(cx - radius * 0.62f, bodyCy + radius * 0.52f),
                size = Size(radius * 1.24f, radius * 0.32f),
            )
            drawRect(
                color = Color(0xFFC2305F),
                topLeft = Offset(cx + radius * 0.22f, bodyCy + radius * 0.66f),
                size = Size(radius * 0.18f, radius * 0.42f),
            )
        }
    }
}

private fun DrawScope.drawEffect(styleId: String, cx: Float, bodyCy: Float, radius: Float, phase: Float) {
    val particle = radius * 0.14f
    repeat(EFFECT_PARTICLES) { i ->
        val angle = phase / 2f + i * (TWO_PI / EFFECT_PARTICLES)
        val px = cx + cos(angle) * radius * 1.22f
        val py = bodyCy + sin(angle) * radius * 1.12f
        if (styleId == "hearts") {
            val heart = Color(0xFFFF6F91)
            drawCircle(heart, particle * 0.45f, Offset(px - particle * 0.35f, py))
            drawCircle(heart, particle * 0.45f, Offset(px + particle * 0.35f, py))
            drawPath(
                Path().apply {
                    moveTo(px - particle * 0.78f, py + particle * 0.12f)
                    lineTo(px + particle * 0.78f, py + particle * 0.12f)
                    lineTo(px, py + particle)
                    close()
                },
                color = heart,
            )
        } else {
            val star = Color(0xFFFFC94D)
            val width = radius * 0.05f
            drawLine(star, Offset(px - particle, py), Offset(px + particle, py), width, StrokeCap.Round)
            drawLine(star, Offset(px, py - particle), Offset(px, py + particle), width, StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawMusicNote(x: Float, y: Float, radius: Float, phase: Float, moving: Boolean) {
    val lift = if (moving) sin(phase * 2f) * radius * 0.1f else 0f
    val note = Color(0xFF5B47F0)
    val head = radius * 0.13f
    drawOval(note, Offset(x - head, y + lift), Size(head * 2f, head * 1.5f))
    drawLine(note, Offset(x + head * 0.9f, y + lift + head * 0.6f), Offset(x + head * 0.9f, y + lift - radius * 0.45f), radius * 0.05f)
    drawLine(
        note,
        Offset(x + head * 0.9f, y + lift - radius * 0.45f),
        Offset(x + head * 2.6f, y + lift - radius * 0.32f),
        radius * 0.05f,
        StrokeCap.Round,
    )
}

private fun mix(a: Color, b: Color, amount: Float): Color = Color(
    red = a.red + (b.red - a.red) * amount,
    green = a.green + (b.green - a.green) * amount,
    blue = a.blue + (b.blue - a.blue) * amount,
    alpha = a.alpha,
)

private val DefaultBody = Color(0xFF8E7BFF)
private val Shade = Color(0xFF2A1F66)
private val Ink = Color(0xFF241A3D)
private val Cheek = Color(0x73FF8FA3)
private val GalaxySpecks = listOf(-0.45f to 0.25f, 0.35f to 0.5f, 0.05f to 0.72f, 0.6f to 0.05f, -0.2f to 0.55f)

private const val TWO_PI = 6.2831855f
private const val BLINK_THRESHOLD = 0.94f
private const val DANCE_TILT = 6f
private const val EFFECT_PARTICLES = 5
