package com.mision.app.core.gamification

import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.ShopItem

/**
 * Catalog of redeemable cosmetics.
 *
 * The shop is deliberately data driven: adding a new item is a one-line change
 * and the UI, the purchase rules and the pet renderer all pick it up.
 */
object ShopCatalog {

    val items: List<ShopItem> = listOf(
        // Sombreros
        ShopItem("hat_party", "Sombrero de fiesta", "Para celebrar cada misión cumplida.", CosmeticSlot.HAT, 120, "party", "#FF7BAC"),
        ShopItem("hat_astro", "Gorro de astrónomo", "Mira las estrellas desde tu escritorio.", CosmeticSlot.HAT, 180, "astro", "#6C5CE7"),
        ShopItem("hat_crown", "Corona dorada", "Recompensa para quien nunca se rinde.", CosmeticSlot.HAT, 600, "crown", "#F5C542"),
        // Accesorios
        ShopItem("acc_glasses", "Gafas de sol", "Estilo inmediato, cero esfuerzo.", CosmeticSlot.ACCESSORY, 150, "glasses", "#2B2D42"),
        ShopItem("acc_scarf", "Bufanda aterciopelada", "Para los días de concentración.", CosmeticSlot.ACCESSORY, 220, "scarf", "#E5477E"),
        // Fondos
        ShopItem("bg_dawn", "Amanecer", "Empieza el día con luz propia.", CosmeticSlot.BACKGROUND, 200, "dawn", "#FFD3A5"),
        ShopItem("bg_nebula", "Nebulosa", "Un rincón tranquilo del universo.", CosmeticSlot.BACKGROUND, 250, "nebula", "#3B2FA8"),
        ShopItem("bg_forest", "Bosque encantado", "Verde, calma y buen ánimo.", CosmeticSlot.BACKGROUND, 300, "forest", "#22B573"),
        // Colores
        ShopItem("color_mint", "Menta", "Frescor para tu compañero.", CosmeticSlot.COLOR, 90, "mint", "#7CF5C2"),
        ShopItem("color_coral", "Coral", "Calidez en cada gesto.", CosmeticSlot.COLOR, 90, "coral", "#FF8FA3"),
        ShopItem("color_aurora", "Aurora", "Un degradado que no pasa desapercibido.", CosmeticSlot.COLOR, 260, "aurora", "#4CC9F0"),
        // Efectos
        ShopItem("fx_sparkle", "Chispas", "Brilla al completar misiones.", CosmeticSlot.EFFECT, 320, "sparkle", "#FFE27A"),
        ShopItem("fx_hearts", "Corazones", "Amor propio en forma de partículas.", CosmeticSlot.EFFECT, 340, "hearts", "#FF7BAC"),
        // Emotes
        ShopItem("emote_wave", "Saludo", "Un saludo cada vez que entras.", CosmeticSlot.EMOTE, 100, "wave", "#4CC9F0"),
        ShopItem("emote_dance", "Baile feliz", "Baila con cada victoria.", CosmeticSlot.EMOTE, 180, "dance", "#9BE15D"),
        // Pieles
        ShopItem("skin_gold", "Pelaje dorado", "Para perfiles que brillan.", CosmeticSlot.SKIN, 450, "gold", "#F5C542"),
        ShopItem("skin_galaxy", "Galaxia", "Una pieza realmente especial.", CosmeticSlot.SKIN, 700, "galaxy", "#8E7BFF"),
    )

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }

    fun forSlot(slot: CosmeticSlot): List<ShopItem> = items.filter { it.category == slot }

    val categoryOrder: List<CosmeticSlot> = listOf(
        CosmeticSlot.HAT,
        CosmeticSlot.ACCESSORY,
        CosmeticSlot.COLOR,
        CosmeticSlot.BACKGROUND,
        CosmeticSlot.EFFECT,
        CosmeticSlot.EMOTE,
        CosmeticSlot.SKIN,
    )
}
