package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.ShopRepository
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
    val visibleItems: List<ShopItem>
        get() = if (selectedSlot == null) items else items.filter { it.category == selectedSlot }

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
            equippedIds = equipped.encode()
                .split(";")
                .mapNotNull { part ->
                    part.substringAfter(':', "").takeIf { it.isNotBlank() }
                }
                .toSet(),
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
                val state = uiState.value
                val result = if (state.isOwned(item)) {
                    val equipResult = useCases.equipReward(item.id)
                    _message.value = when (equipResult) {
                        com.mision.app.domain.usecase.EquipResult.Equipped ->
                            "¡${item.name} equipado!"
                        com.mision.app.domain.usecase.EquipResult.Removed ->
                            "Has quitado ${item.name}."
                        com.mision.app.domain.usecase.EquipResult.NotOwned ->
                            "Completa más misiones para poder usarlo."
                    }
                    null
                } else {
                    useCases.purchaseReward(item).result
                }
                if (result != null) {
                    _message.value = when (result) {
                        is PurchaseResult.Success ->
                            "¡${item.name} comprado! Te quedan ${result.remainingCoins} monedas."
                        is PurchaseResult.AlreadyOwned ->
                            "Ya tienes ${item.name}. Puedes equiparlo cuando quieras."
                        is PurchaseResult.NotEnoughCoins ->
                            "Te faltan ${result.missing} monedas. Completa misiones para conseguir más."
                        is PurchaseResult.UnknownItem ->
                            "Este artículo ya no está disponible."
                    }
                }
            } finally {
                _busyItemId.value = null
            }
        }
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
