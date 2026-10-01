package com.mision.app.presentation.viewmodel

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
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One product card. */
data class ShopItemUi(
    val item: ShopItem,
    val owned: Boolean,
    val equipped: Boolean,
    val missingCoins: Int,
) {
    val affordable: Boolean get() = missingCoins == 0
}

data class ShopUiState(
    val isLoading: Boolean = true,
    val coins: Int = 0,
    val ownedCount: Int = 0,
    val totalCount: Int = 0,
    val categories: List<CosmeticSlot> = ShopCatalog.categoryOrder,
    val selectedCategory: CosmeticSlot? = null,
    /** Items of the selected category (all when null), in catalogue order. */
    val items: List<ShopItemUi> = emptyList(),
)

/** Tienda: catalogue, balance, category filter, purchases and equipping. */
class ShopViewModel(
    private val useCases: UseCases,
    shopRepository: ShopRepository,
    progressRepository: ProgressRepository,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<CosmeticSlot?>(null)
    private val _busyItemId = MutableStateFlow<String?>(null)
    private val _pendingPurchase = MutableStateFlow<ShopItemUi?>(null)

    /** Item whose action is running (its button shows progress). */
    val busyItemId: StateFlow<String?> = _busyItemId.asStateFlow()

    /** Item waiting for the purchase confirmation dialog. */
    val pendingPurchase: StateFlow<ShopItemUi?> = _pendingPurchase.asStateFlow()

    val messages = UserMessages()

    val uiState: StateFlow<ShopUiState> = combine(
        shopRepository.observeCatalog(),
        shopRepository.observePurchases(),
        shopRepository.observeEquipped(),
        progressRepository.observeProfile(),
        selectedCategory,
    ) { catalog, purchases, equipped, profile, category ->
        val equippedIds = equipped.ids
        val order = ShopCatalog.categoryOrder
        val items = catalog
            .filter { category == null || it.category == category }
            .sortedWith(compareBy({ order.indexOf(it.category) }, { it.cost }))
            .map { item ->
                val owned = item.id in purchases
                ShopItemUi(
                    item = item,
                    owned = owned,
                    equipped = item.id in equippedIds,
                    missingCoins = if (owned) 0 else (item.cost - profile.coins).coerceAtLeast(0),
                )
            }
        ShopUiState(
            isLoading = false,
            coins = profile.coins,
            ownedCount = catalog.count { it.id in purchases },
            totalCount = catalog.size,
            selectedCategory = category,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShopUiState(),
    )

    fun onCategorySelected(category: CosmeticSlot?) {
        selectedCategory.value = category
    }

    /** Owned items toggle on the pet; others ask for confirmation first. */
    fun onItemAction(entry: ShopItemUi) {
        when {
            entry.owned -> toggleEquipped(entry.item)
            entry.affordable -> _pendingPurchase.value = entry
            else -> messages.show("Te faltan ${entry.missingCoins} monedas. Completa misiones para conseguirlas.")
        }
    }

    fun dismissPurchase() {
        _pendingPurchase.value = null
    }

    fun confirmPurchase() {
        val entry = _pendingPurchase.value ?: return
        _pendingPurchase.value = null
        runAction(entry.item) {
            val outcome = useCases.purchaseReward(entry.item)
            val text = when (val result = outcome.result) {
                is PurchaseResult.Success -> "¡${entry.item.name} es tuyo! Te quedan ${result.remainingCoins} monedas."
                is PurchaseResult.AlreadyOwned -> "Ya tienes ${entry.item.name}."
                is PurchaseResult.NotEnoughCoins -> "Te faltan ${result.missing} monedas."
            }
            val achievements = outcome.newAchievements.joinToString { it.name }
            messages.show(if (achievements.isEmpty()) text else "$text Logro desbloqueado: $achievements.")
        }
    }

    private fun toggleEquipped(item: ShopItem) = runAction(item) {
        messages.show(
            when (useCases.equipReward(item)) {
                EquipResult.Equipped -> "${item.name} equipado."
                EquipResult.Removed -> "Has quitado ${item.name}."
                EquipResult.NotOwned -> "Primero tienes que conseguir ${item.name}."
            },
        )
    }

    private fun runAction(item: ShopItem, action: suspend () -> Unit) {
        if (_busyItemId.value != null) return
        _busyItemId.value = item.id
        viewModelScope.launch {
            runCatching { action() }.onFailure { messages.show(GENERIC_ERROR) }
            _busyItemId.value = null
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
