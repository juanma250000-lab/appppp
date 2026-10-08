package com.mision.app.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.domain.model.ShopItem
import com.mision.app.presentation.theme.Dimens

/**
 * Product image of a shop item: the pet wearing only that cosmetic, drawn by
 * the same renderer the pet screen uses, so the preview is exactly what the
 * user gets. Being vector based it stays sharp at any card size and needs no
 * bitmap assets or network.
 *
 * The square frame keeps every card's image the same proportion; [overlay]
 * hosts badges pinned to the frame (e.g. "Equipado").
 */
@Composable
fun ShopItemArt(
    item: ShopItem,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val accent = parseHexColor(item.previewColorHex, MaterialTheme.colorScheme.primary)
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
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.38f), accent.copy(alpha = 0.10f)),
                ),
                shape,
            )
            .semantics { contentDescription = "Vista previa de ${item.name} en tu mascota" },
        contentAlignment = Alignment.Center,
    ) {
        StaticPet(pet = previewPet, modifier = Modifier.fillMaxSize(0.88f))
        overlay()
    }
}
