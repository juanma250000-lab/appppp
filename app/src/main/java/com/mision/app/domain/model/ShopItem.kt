package com.mision.app.domain.model

/** Static definition of a redeemable cosmetic. */
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val category: CosmeticSlot,
    val cost: Int,
    /** Palette / style identifier consumed by the pet renderer. */
    val styleId: String,
    /** Fallback color used for previews and swatches. */
    val previewColorHex: String,
)

/** Result of trying to buy an item. */
sealed interface PurchaseResult {
    data class Success(val item: ShopItem, val remainingCoins: Int) : PurchaseResult
    data object AlreadyOwned : PurchaseResult
    data class NotEnoughCoins(val missing: Int) : PurchaseResult
}
