package com.mision.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.domain.model.ShopItem
import com.mision.app.presentation.theme.Dimens

/**
 * Product image of a shop item: Nube wearing only that cosmetic, drawn by
 * the same renderer the pet screen uses, so the picture is exactly what the
 * user gets. Being vector based it stays sharp at any card size and needs no
 * bitmap assets or network.
 *
 * The square "patch of sky" keeps every card's image the same proportion and
 * is tinted with the item's own colour; [overlay] hosts badges pinned to the
 * frame (e.g. "Equipado").
 */
@Composable
fun ShopItemArt(
    item: ShopItem,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val accent = parseHexColor(item.previewColorHex, MaterialTheme.colorScheme.primary)
    val sky = MaterialTheme.colorScheme.primaryContainer
    val dark = isDarkSurface()
    val previewPet = remember(item.id) {
        Pet(
            name = item.name,
            xp = 0,
            happiness = 100,
            energy = 100,
            mood = PetMood.FELIZ,
            equipped = EquippedCosmetics().with(item.category, item.id),
            lastInteractionEpochDay = 0,
        )
    }
    val shape = RoundedCornerShape(Dimens.RadiusSm)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .drawBehind { drawSkyPatch(accent, sky, dark) }
            .semantics { contentDescription = "Vista previa de ${item.name} en tu mascota" },
        contentAlignment = Alignment.Center,
    ) {
        StaticPet(pet = previewPet, modifier = Modifier.fillMaxSize(0.94f))
        overlay()
    }
}

/** Tinted sky with two faint background clouds, echoing the mascot. */
private fun DrawScope.drawSkyPatch(accent: Color, sky: Color, dark: Boolean) {
    val top = mixColors(sky, accent, if (dark) 0.40f else 0.30f).copy(alpha = if (dark) 0.55f else 0.85f)
    val bottom = mixColors(sky, accent, 0.10f).copy(alpha = if (dark) 0.25f else 0.55f)
    drawRect(Brush.verticalGradient(listOf(top, bottom)))
    val puff = Color.White.copy(alpha = if (dark) 0.10f else 0.45f)
    val w = size.width
    val h = size.height
    val puffs = Path()
    listOf(
        Offset(w * 0.16f, h * 0.18f) to w * 0.07f,
        Offset(w * 0.25f, h * 0.15f) to w * 0.09f,
        Offset(w * 0.34f, h * 0.19f) to w * 0.06f,
        Offset(w * 0.78f, h * 0.84f) to w * 0.06f,
        Offset(w * 0.86f, h * 0.81f) to w * 0.08f,
        Offset(w * 0.94f, h * 0.85f) to w * 0.05f,
    ).forEach { (center, radius) -> puffs.addOval(Rect(center, radius)) }
    // One path, so overlapping puffs read as a single cloud instead of bubbles.
    drawPath(puffs, puff)
}
