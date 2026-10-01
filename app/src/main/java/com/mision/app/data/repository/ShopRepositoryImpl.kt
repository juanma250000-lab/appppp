package com.mision.app.data.repository

import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.core.time.ClockProvider
import com.mision.app.data.local.ProgressDao
import com.mision.app.data.local.PurchaseEntity
import com.mision.app.data.toEntity
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ShopRepositoryImpl(
    private val dao: ProgressDao,
    private val clock: ClockProvider,
) : ShopRepository {

    override fun observeCatalog(): Flow<List<ShopItem>> = flowOf(ShopCatalog.items)

    override fun observePurchases(): Flow<Set<String>> =
        dao.observePurchases().map { rows -> rows.map { it.itemId }.toSet() }

    override suspend fun getPurchases(): Set<String> =
        dao.getPurchasedItemIds().toSet()

    override suspend fun recordPurchase(itemId: String, atEpochSecond: Long) {
        dao.insertPurchase(
            PurchaseEntity(
                itemId = itemId,
                purchasedAtEpochSecond = if (atEpochSecond > 0) atEpochSecond else clock.nowEpochSecond(),
            ),
        )
    }

    override fun observeEquipped(): Flow<EquippedCosmetics> =
        dao.observePet().map { EquippedCosmetics.decode(it?.equippedCosmetics.orEmpty()) }

    override suspend fun equip(itemId: String) {
        val item = ShopCatalog.byId(itemId) ?: return
        // The pet row is created lazily; equipping must work even before the
        // first save so the button never silently does nothing.
        val today = clock.todayEpochDay()
        val pet = dao.getPet() ?: Pet.default(epochDay = today).toEntity(createdAtEpochDay = today)
        val equipped = EquippedCosmetics.decode(pet.equippedCosmetics)
        val current = equipped.idFor(item.category)
        dao.upsertPet(
            pet.copy(
                equippedCosmetics = equipped
                    .with(item.category, if (current == itemId) null else itemId)
                    .encode(),
            ),
        )
    }

    override suspend fun purchaseResult(item: ShopItem, currentCoins: Int): PurchaseResult {
        val owned = getPurchases()
        return when {
            item.id in owned -> PurchaseResult.AlreadyOwned
            currentCoins < item.cost -> PurchaseResult.NotEnoughCoins(item.cost - currentCoins)
            else -> PurchaseResult.Success(item, currentCoins - item.cost)
        }
    }
}
