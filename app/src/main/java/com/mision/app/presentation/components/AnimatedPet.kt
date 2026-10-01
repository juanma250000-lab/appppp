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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.presentation.theme.Dimens
import kotlin.math.sin

/**
 * The virtual pet, drawn entirely with Compose primitives so it stays crisp at
 * any size and can wear every cosmetic from the shop.
 *
 * Idle animation (bob + blink) can be disabled globally from settings and is
 * automatically reduced for users that ask the system to disable animations.
 */
@Composable
fun AnimatedPet(
    pet: Pet,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    animate: Boolean = true,
    accessibilityLabel: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "petIdle")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Dimens.AnimPetIdle * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "petTime",
    )
    val blink by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "petBlink",
    )

    val label = accessibilityLabel
        ?: "Mascota ${pet.name}, ${pet.mood.displayName.lowercase()}"

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = label },
    ) {
        val t = if (animate) time else 0f
        val blinking = if (animate) blink > 0.94f else false
        drawPet(
            pet = pet,
            bobOffset = (sin(t) * 4.dp.toPx()),
            blink = blinking,
            celebrate = animate,
            phase = t,
        )
    }
}

private fun DrawScope.drawPet(
    pet: Pet,
    bobOffset: Float,
    blink: Boolean,
    celebrate: Boolean,
    phase: Float,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val bodyRadius = w * 0.30f
    val bodyCy = h * 0.54f + bobOffset

    // --- Ground shadow ----------------------------------------------------
    drawOval(
        color = Color.Black.copy(alpha = 0.16f),
        topLeft = Offset(cx - bodyRadius * 0.85f, h * 0.86f),
        size = Size(bodyRadius * 1.7f, bodyRadius * 0.28f),
    )

    // --- Background halo (Fondos) ----------------------------------------
    val backgroundStyle = pet.equipped.background
    if (backgroundStyle != null) {
        val halo = parseHexColor(
            ShopCatalog.byId(backgroundStyle)?.previewColorHex ?: "#4CC9F0",
            Color(0xFF4CC9F0),
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(halo.copy(alpha = 0.55f), halo.copy(alpha = 0f)),
            ),
            radius = w * 0.48f,
            center = Offset(cx, bodyCy),
        )
    }

    // --- Ears --------------------------------------------------------------
    val palette = bodyPalette(pet.equipped)
    val earRadius = bodyRadius * 0.42f
    val earY = bodyCy - bodyRadius * 0.78f
    drawCircle(color = palette[2], radius = earRadius, center = Offset(cx - bodyRadius * 0.72f, earY))
    drawCircle(color = palette[2], radius = earRadius, center = Offset(cx + bodyRadius * 0.72f, earY))
    drawCircle(color = palette[1], radius = earRadius * 0.55f, center = Offset(cx - bodyRadius * 0.72f, earY + earRadius * 0.18f))
    drawCircle(color = palette[1], radius = earRadius * 0.55f, center = Offset(cx + bodyRadius * 0.72f, earY + earRadius * 0.18f))

    // --- Body --------------------------------------------------------------
    val pulse = if (celebrate && pet.mood == PetMood.CELEBRANDO) {
        1f + sin(phase * 3f) * 0.02f
    } else {
        1f
    }
    val radius = bodyRadius * pulse
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(palette[0], palette[1], palette[2]),
            startY = bodyCy - radius,
            endY = bodyCy + radius,
        ),
        radius = radius,
        center = Offset(cx, bodyCy),
    )
    // Glossy highlight (the "glass" cue on the pet itself).
    drawOval(
        color = Color.White.copy(alpha = 0.22f),
        topLeft = Offset(cx - radius * 0.55f, bodyCy - radius * 0.78f),
        size = Size(radius * 0.75f, radius * 0.34f),
    )

    // --- Face --------------------------------------------------------------
    val eyeColor = Color(0xFF241A3D)
    val eyeOffsetX = radius * 0.36f
    val eyeY = bodyCy - radius * 0.10f
    val eyeRadius = radius * 0.145f
    val eyeHeight = if (blink) eyeRadius * 0.16f else eyeRadius * 2f
    listOf(-1f, 1f).forEach { side ->
        drawOval(
            color = eyeColor,
            topLeft = Offset(cx + side * eyeOffsetX - eyeRadius, eyeY - eyeHeight / 2f),
            size = Size(eyeRadius * 2f, eyeHeight),
        )
    }
    if (!blink) {
        listOf(-1f, 1f).forEach { side ->
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.32f,
                center = Offset(cx + side * eyeOffsetX + eyeRadius * 0.3f, eyeY - eyeRadius * 0.35f),
            )
        }
    }

    // Cheeks.
    val cheek = Color(0xFFFF8FA3).copy(alpha = 0.45f)
    drawCircle(color = cheek, radius = radius * 0.12f, center = Offset(cx - radius * 0.55f, eyeY + radius * 0.30f))
    drawCircle(color = cheek, radius = radius * 0.12f, center = Offset(cx + radius * 0.55f, eyeY + radius * 0.30f))

    // Mouth.
    val mouthWidth = radius * 0.52f
    val mouthHeight = radius * 0.34f
    val mouthTop = eyeY + radius * 0.34f
    when (pet.mood) {
        PetMood.CELEBRANDO -> drawOval(
            color = eyeColor,
            topLeft = Offset(cx - mouthWidth / 2f, mouthTop),
            size = Size(mouthWidth, mouthHeight),
        )
        PetMood.TRISTE, PetMood.CANSADO -> drawArc(
            color = eyeColor,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - mouthWidth / 2f, mouthTop - mouthHeight / 2f),
            size = Size(mouthWidth, mouthHeight),
            style = Stroke(width = radius * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
        else -> drawArc(
            color = eyeColor,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - mouthWidth / 2f, mouthTop - mouthHeight / 2f),
            size = Size(mouthWidth, mouthHeight),
            style = Stroke(width = radius * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }

    // --- Cosmetics ---------------------------------------------------------
    pet.equipped.hat?.let { drawHat(it, cx, bodyCy, radius, palette) }
    pet.equipped.accessory?.let { drawAccessory(it, cx, eyeY, radius) }
    pet.equipped.effect?.let { drawEffect(it, cx, bodyCy, radius, phase) }
}

private fun bodyPalette(equipped: EquippedCosmetics): List<Color> {
    val colorHex = equipped.color?.let { ShopCatalog.byId(it)?.previewColorHex }
    val skinHex = equipped.skin?.let { ShopCatalog.byId(it)?.previewColorHex }
    val base = parseHexColor(
        colorHex ?: skinHex ?: "#8E7BFF",
        Color(0xFF8E7BFF),
    )
    val light = mix(base, Color.White, 0.35f)
    val dark = mix(base, Color(0xFF2A1F66), 0.35f)
    return listOf(light, base, dark)
}

private fun mix(a: Color, b: Color, amount: Float): Color = Color(
    red = a.red + (b.red - a.red) * amount,
    green = a.green + (b.green - a.green) * amount,
    blue = a.blue + (b.blue - a.blue) * amount,
    alpha = a.alpha,
)

private fun DrawScope.drawHat(styleId: String, cx: Float, bodyCy: Float, radius: Float, palette: List<Color>) {
    val top = bodyCy - radius
    when (styleId) {
        "party" -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - radius * 0.45f, top + radius * 0.16f)
                lineTo(cx, top - radius * 0.75f)
                lineTo(cx + radius * 0.45f, top + radius * 0.16f)
                close()
            }
            drawPath(path, color = Color(0xFFFF7BAC))
            drawCircle(color = Color(0xFFFFE27A), radius = radius * 0.13f, center = Offset(cx, top - radius * 0.78f))
        }
        "astro" -> {
            drawArc(
                color = Color(0xFF6C5CE7),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - radius * 0.55f, top - radius * 0.32f),
                size = Size(radius * 1.1f, radius * 0.75f),
            )
            drawOval(
                color = Color(0xFFCFEAFF).copy(alpha = 0.85f),
                topLeft = Offset(cx - radius * 0.32f, top - radius * 0.18f),
                size = Size(radius * 0.64f, radius * 0.30f),
            )
        }
        "crown" -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - radius * 0.52f, top + radius * 0.14f)
                lineTo(cx - radius * 0.52f, top - radius * 0.42f)
                lineTo(cx - radius * 0.24f, top - radius * 0.12f)
                lineTo(cx, top - radius * 0.52f)
                lineTo(cx + radius * 0.24f, top - radius * 0.12f)
                lineTo(cx + radius * 0.52f, top - radius * 0.42f)
                lineTo(cx + radius * 0.52f, top + radius * 0.14f)
                close()
            }
            drawPath(path, color = Color(0xFFF5C542))
            drawPath(path, color = Color(0x55FFFFFF), style = Stroke(width = radius * 0.05f))
        }
        else -> drawRect(
            color = palette[1],
            topLeft = Offset(cx - radius * 0.5f, top - radius * 0.05f),
            size = Size(radius, radius * 0.25f),
        )
    }
}

private fun DrawScope.drawAccessory(styleId: String, cx: Float, eyeY: Float, radius: Float) {
    when (styleId) {
        "glasses" -> {
            val lensRadius = radius * 0.24f
            val offset = radius * 0.36f
            val stroke = Stroke(width = radius * 0.06f)
            drawCircle(Color(0xFF2B2D42), lensRadius, Offset(cx - offset, eyeY), style = stroke)
            drawCircle(Color(0xFF2B2D42), lensRadius, Offset(cx + offset, eyeY), style = stroke)
            drawLine(
                color = Color(0xFF2B2D42),
                start = Offset(cx - offset + lensRadius, eyeY),
                end = Offset(cx + offset - lensRadius, eyeY),
                strokeWidth = radius * 0.06f,
            )
        }
        "scarf" -> drawOval(
            color = Color(0xFFE5477E),
            topLeft = Offset(cx - radius * 0.55f, eyeY + radius * 0.78f),
            size = Size(radius * 1.1f, radius * 0.34f),
        )
        else -> Unit
    }
}

private fun DrawScope.drawEffect(styleId: String, cx: Float, bodyCy: Float, radius: Float, phase: Float) {
    for (i in 0 until 5) {
        val angle = phase / 2f + i * (6.283185f / 5f)
        val px = cx + kotlin.math.cos(angle) * radius * 1.18f
        val py = bodyCy + kotlin.math.sin(angle) * radius * 1.10f
        val sparkle = radius * 0.14f
        if (styleId == "hearts") {
            val heart = Color(0xFFFF7BAC)
            drawCircle(heart, sparkle * 0.45f, Offset(px - sparkle * 0.35f, py))
            drawCircle(heart, sparkle * 0.45f, Offset(px + sparkle * 0.35f, py))
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(px - sparkle * 0.78f, py + sparkle * 0.12f)
                lineTo(px + sparkle * 0.78f, py + sparkle * 0.12f)
                lineTo(px, py + sparkle * 1.0f)
                close()
            }
            drawPath(path, color = heart)
        } else {
            val star = Color(0xFFFFE27A)
            drawLine(star, Offset(px - sparkle, py), Offset(px + sparkle, py), strokeWidth = radius * 0.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(star, Offset(px, py - sparkle), Offset(px, py + sparkle), strokeWidth = radius * 0.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}
