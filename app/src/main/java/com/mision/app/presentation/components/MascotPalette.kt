package com.mision.app.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.PetMood

/** Surface treatment a skin adds on top of the body colours. */
enum class MascotFinish { PLAIN, GOLD, GALAXY, AURORA }

/**
 * Colours of Nube's body. The defaults are sampled from the supplied
 * illustration: blue outline, pale blue lower rim and a white puffy body.
 */
@Immutable
data class MascotPalette(
    val outline: Color,
    val rimTop: Color,
    val rimBottom: Color,
    val bodyTop: Color,
    val bodyBottom: Color,
    val finish: MascotFinish = MascotFinish.PLAIN,
) {
    companion object {
        val Default = MascotPalette(
            outline = Color(0xFF3A71B4),
            rimTop = Color(0xFFE3EEFB),
            rimBottom = Color(0xFFBBD7F5),
            bodyTop = Color(0xFFFFFFFF),
            bodyBottom = Color(0xFFF2F7FE),
        )
    }
}

/**
 * Body colours for the equipped cosmetics. A colour item always wins over a
 * skin's tint (the skin keeps its surface finish), matching how the shop has
 * always combined both slots.
 */
fun mascotPaletteFor(equipped: EquippedCosmetics, mood: PetMood = PetMood.FELIZ): MascotPalette {
    val colorItem = equipped.color?.let(ShopCatalog::byId)
    val skinItem = equipped.skin?.let(ShopCatalog::byId)

    val finish = when {
        colorItem?.styleId == "aurora" -> MascotFinish.AURORA
        skinItem?.styleId == "gold" -> MascotFinish.GOLD
        skinItem?.styleId == "galaxy" -> MascotFinish.GALAXY
        else -> MascotFinish.PLAIN
    }

    val base = when {
        colorItem != null -> tinted(hexToColor(colorItem.previewColorHex, MascotPalette.Default.outline))
        skinItem?.styleId == "galaxy" -> galaxyPalette()
        skinItem != null -> tinted(hexToColor(skinItem.previewColorHex, MascotPalette.Default.outline))
        else -> MascotPalette.Default
    }.copy(finish = finish)

    // A sad cloud turns a little grey: the mood reads without any text.
    return if (mood == PetMood.TRISTE) base.greyed() else base
}

private fun galaxyPalette() = MascotPalette(
    outline = Color(0xFF1A1450),
    rimTop = Color(0xFF8E7BFF),
    rimBottom = Color(0xFF241C6E),
    bodyTop = Color(0xFF7A68E8),
    bodyBottom = Color(0xFF3A2E9C),
    finish = MascotFinish.GALAXY,
)

private fun tinted(tint: Color) = MascotPalette(
    outline = mixColors(tint, Color(0xFF14204A), 0.45f),
    rimTop = mixColors(Color.White, tint, 0.45f),
    rimBottom = mixColors(tint, Color.White, 0.10f),
    bodyTop = mixColors(Color.White, tint, 0.18f),
    bodyBottom = mixColors(Color.White, tint, 0.40f),
)

private fun MascotPalette.greyed(): MascotPalette {
    val grey = Color(0xFF98A4B6)
    return copy(
        rimTop = mixColors(rimTop, grey, 0.25f),
        rimBottom = mixColors(rimBottom, grey, 0.30f),
        bodyTop = mixColors(bodyTop, grey, 0.18f),
        bodyBottom = mixColors(bodyBottom, grey, 0.25f),
    )
}

/** Linear blend between two colours; [amount] 0 keeps [a], 1 returns [b]. */
fun mixColors(a: Color, b: Color, amount: Float): Color = Color(
    red = a.red + (b.red - a.red) * amount,
    green = a.green + (b.green - a.green) * amount,
    blue = a.blue + (b.blue - a.blue) * amount,
    alpha = a.alpha + (b.alpha - a.alpha) * amount,
)

/**
 * Parses "#RRGGBB" / "#AARRGGBB" without Android APIs, so the palette logic
 * stays testable on the JVM.
 */
fun hexToColor(hex: String, fallback: Color): Color {
    val clean = hex.trim().removePrefix("#")
    val value = clean.toLongOrNull(16) ?: return fallback
    return when (clean.length) {
        6 -> Color(0xFF000000 or value)
        8 -> Color(value)
        else -> fallback
    }
}
