package com.mision.app.presentation.viewmodel

import com.mision.app.core.text.plural
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.ShopRepository
import com.mision.app.domain.usecase.EquipResult
import com.mision.app.domain.usecase.PurchaseRewardResult
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShopUiState(
    val isLoading: Boolean = true,
    val items: List<ShopItem> = emptyList(),
    val ownedIds: Set<String> = emptySet(),
    val equippedIds: Set<String> = emptySet(),
    val coins: Int = 0,
    val selectedSlot: CosmeticSlot? = null,
) {
    /** Items of the selected category, grouped in the same order as the filter chips. */
    val visibleItems: List<ShopItem> by lazy {
        val filtered = if (selectedSlot == null) items else items.filter { it.category == selectedSlot }
        filtered.sortedBy { ShopCatalog.categoryOrder.indexOf(it.category) }
    }

    /** How many catalogue items the user already owns. */
    val ownedCount: Int by lazy { items.count { it.id in ownedIds } }

    fun isOwned(item: ShopItem): Boolean = item.id in ownedIds
    fun isEquipped(item: ShopItem): Boolean = item.id in equippedIds
    fun canAfford(item: ShopItem): Boolean = coins >= item.cost
}

/** Tienda screen: catalogue, balances, filters and purchase rules. */
class ShopViewModel(
    private val useCases: UseCases,
    private val shopRepository: ShopRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val _slot = MutableStateFlow<CosmeticSlot?>(null)
    private val _busyItemId = MutableStateFlow<String?>(null)
    private val _message = MutableStateFlow<String?>(null)

    val message: StateFlow<String?> = _message
    val busyItemId: StateFlow<String?> = _busyItemId

    val uiState: StateFlow<ShopUiState> = combine(
        shopRepository.observeCatalog(),
        shopRepository.observePurchases(),
        shopRepository.observeEquipped(),
        progressRepository.observeProfile(),
        _slot,
    ) { catalog, purchases, equipped, profile, slot ->
        ShopUiState(
            isLoading = false,
            items = catalog,
            ownedIds = purchases,
            equippedIds = CosmeticSlot.entries.mapNotNull(equipped::idFor).toSet(),
            coins = profile.coins,
            selectedSlot = slot,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopUiState(),
    )

    fun onSlotSelected(slot: CosmeticSlot?) {
        _slot.value = slot
    }

    fun dismissMessage() {
        _message.value = null
    }

    /** Buys the item when possible; otherwise explains why it did not happen. */
    fun onItemAction(item: ShopItem) {
        if (_busyItemId.value != null) return
        viewModelScope.launch {
            _busyItemId.value = item.id
            try {
                _message.value = if (uiState.value.isOwned(item)) {
                    when (useCases.equipReward(item.id)) {
                        EquipResult.Equipped -> "¡${item.name} equipado!"
                        EquipResult.Removed -> "Has quitado ${item.name}."
                        EquipResult.NotOwned -> "Completa más misiones para poder usarlo."
                    }
                } else {
                    purchaseMessage(item, useCases.purchaseReward(item))
                }
            } finally {
                _busyItemId.value = null
            }
        }
    }

    private fun purchaseMessage(item: ShopItem, purchase: PurchaseRewardResult): String =
        when (val result = purchase.result) {
            is PurchaseResult.Success -> buildString {
                append("¡${item.name} comprado! Ya puedes equiparlo a tu mascota.")
                // Achievements unlocked by the purchase pay coins too, so the
                // balance is reported after those rewards.
                purchase.newAchievements.forEach { achievement ->
                    append("\n\nLogro desbloqueado: ${achievement.name} (+${achievement.rewardCoins} monedas).")
                }
                val bonus = purchase.newAchievements.sumOf { it.rewardCoins }
                append("\n\nTe quedan ${plural(result.remainingCoins + bonus, "moneda", "monedas")}.")
            }
            is PurchaseResult.AlreadyOwned ->
                "Ya tienes ${item.name}. Puedes equiparlo cuando quieras."
            is PurchaseResult.NotEnoughCoins ->
                "Te faltan ${plural(result.missing, "moneda", "monedas")}. Completa misiones para conseguir más."
            is PurchaseResult.UnknownItem ->
                "Este artículo ya no está disponible."
        }

    companion object {
        fun factory(
            useCases: UseCases,
            shopRepository: ShopRepository,
            progressRepository: ProgressRepository,
        ) = viewModelFactory {
            initializer {
                ShopViewModel(
                    useCases = useCases,
                    shopRepository = shopRepository,
                    progressRepository = progressRepository,
                )
            }
        }
    }
}
