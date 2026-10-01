package com.mision.app.data.repository

import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.core.time.ClockProvider
import com.mision.app.data.local.PurchaseEntity
import com.mision.app.data.local.ProgressDao
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShopRepositoryImpl(
    private val dao: ProgressDao,
    private val clock: ClockProvider,
) : ShopRepository {

    override fun observeCatalog(): Flow<List<ShopItem>> =
        kotlinx.coroutines.flow.flowOf(ShopCatalog.items)

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
        dao.observePet().map { (it?.equippedCosmetics ?: "").let(EquippedCosmetics::decode) }

    override suspend fun equip(itemId: String) {
        val pet = dao.getPet() ?: return
        val item = ShopCatalog.byId(itemId) ?: return
        val equipped = EquippedCosmetics.decode(pet.equippedCosmetics)
        val slot: CosmeticSlot = item.category
        val current = equipped.idFor(slot)
        dao.upsertPet(
            pet.copy(
                equippedCosmetics = equipped
                    .with(slot, if (current == itemId) null else itemId)
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
