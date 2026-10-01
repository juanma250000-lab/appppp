package com.mision.app.domain.usecase

import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.AchievementDefinition
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.ShopRepository
import com.mision.app.domain.repository.TransactionRunner
import kotlinx.coroutines.flow.first

data class PurchaseRewardResult(
    val result: PurchaseResult,
    val newAchievements: List<AchievementDefinition> = emptyList(),
)

/**
 * Buys a shop item: validates ownership and balance, deducts the coins and
 * records the purchase in a single transaction. No real money is ever involved.
 */
class PurchaseRewardUseCase(
    private val shopRepository: ShopRepository,
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val transaction: TransactionRunner,
    private val clock: ClockProvider,
) {
    suspend operator fun invoke(item: ShopItem): PurchaseRewardResult = transaction {
        val profile = progressRepository.getProfile()
        val decision = shopRepository.purchaseResult(item, profile.coins)
        if (decision !is PurchaseResult.Success) return@transaction PurchaseRewardResult(decision)

        shopRepository.recordPurchase(itemId = item.id, atEpochSecond = clock.nowEpochSecond())
        progressRepository.saveProfile(profile.copy(coins = decision.remainingCoins))

        val newAchievements = AchievementEffects.newlyUnlocked(
            progressRepository = progressRepository,
            gamificationRepository = gamificationRepository,
            shopRepository = shopRepository,
            clock = clock,
        )
        grantAchievementRewards(newAchievements)

        PurchaseRewardResult(result = decision, newAchievements = newAchievements)
    }

    /**
     * Unlocked achievements always pay their reward, the same way mission
     * completions do; otherwise buying the fifth item would silently swallow
     * the "Coleccionista" bonus.
     */
    private suspend fun grantAchievementRewards(newAchievements: List<AchievementDefinition>) {
        val xp = newAchievements.sumOf { it.rewardXp }
        val coins = newAchievements.sumOf { it.rewardCoins }
        if (xp == 0 && coins == 0) return
        val current = progressRepository.getProfile()
        progressRepository.saveProfile(
            current.copy(totalXp = current.totalXp + xp, coins = current.coins + coins),
        )
    }
}

sealed interface EquipResult {
    data object Equipped : EquipResult
    data object Removed : EquipResult
    data object NotOwned : EquipResult
}

/** Toggles a purchased cosmetic on the pet (equips or unequips). */
class EquipRewardUseCase(
    private val shopRepository: ShopRepository,
    private val transaction: TransactionRunner,
) {
    suspend operator fun invoke(item: ShopItem): EquipResult = transaction {
        if (item.id !in shopRepository.getPurchases()) return@transaction EquipResult.NotOwned
        val currentlyEquipped = shopRepository.observeEquipped().first().idFor(item.category)
        shopRepository.equip(item.id)
        if (currentlyEquipped == item.id) EquipResult.Removed else EquipResult.Equipped
    }
}
