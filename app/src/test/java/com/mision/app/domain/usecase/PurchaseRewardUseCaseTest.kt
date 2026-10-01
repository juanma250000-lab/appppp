package com.mision.app.domain.usecase

import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.testing.DirectTransactionRunner
import com.mision.app.testing.FakeClock
import com.mision.app.testing.FakeGamificationRepository
import com.mision.app.testing.FakeProgressRepository
import com.mision.app.testing.FakeShopRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PurchaseRewardUseCaseTest {

    private val clock = FakeClock()
    private val progress = FakeProgressRepository()
    private val shop = FakeShopRepository()
    private val gamification = FakeGamificationRepository(progress)
    private lateinit var useCase: PurchaseRewardUseCase

    private val partyHat = ShopCatalog.byId("hat_party")!!
    private val dawn = ShopCatalog.byId("bg_dawn")!!

    @Before
    fun setUp() {
        useCase = PurchaseRewardUseCase(
            shopRepository = shop,
            progressRepository = progress,
            gamificationRepository = gamification,
            transaction = DirectTransactionRunner,
            clock = clock,
        )
    }

    private suspend fun setCoins(coins: Int) =
        progress.saveProfile(progress.getProfile().copy(coins = coins))

    @Test
    fun `an affordable item is bought and the coins are deducted`() = runTest {
        setCoins(200)

        val result = useCase(partyHat)

        assertTrue(result.result is PurchaseResult.Success)
        assertEquals(80, (result.result as PurchaseResult.Success).remainingCoins)
        assertEquals(80, progress.getProfile().coins)
        assertTrue("hat_party" in shop.getPurchases())
        assertTrue(result.newAchievements.isEmpty())
    }

    @Test
    fun `an item cannot be bought without enough coins`() = runTest {
        setCoins(100)

        val result = useCase(partyHat)

        assertTrue(result.result is PurchaseResult.NotEnoughCoins)
        assertEquals(20, (result.result as PurchaseResult.NotEnoughCoins).missing)
        assertEquals(100, progress.getProfile().coins)
        assertTrue(shop.getPurchases().isEmpty())
    }

    @Test
    fun `an owned item is never sold twice`() = runTest {
        setCoins(500)
        shop.seedPurchases(setOf(partyHat.id))

        val result = useCase(partyHat)

        assertTrue(result.result is PurchaseResult.AlreadyOwned)
        assertEquals(500, progress.getProfile().coins)
        assertEquals(1, shop.getPurchases().size)
    }

    @Test
    fun `buying the fifth reward unlocks the collector achievement with its reward`() = runTest {
        setCoins(1000)
        shop.seedPurchases(setOf("hat_party", "hat_astro", "acc_glasses", "acc_scarf"))

        val result = useCase(dawn)

        assertTrue(result.result is PurchaseResult.Success)
        assertEquals(listOf("coleccionista"), result.newAchievements.map { it.id })
        // 1000 - 200 spent, plus the 50 coins of the achievement.
        assertEquals(850, progress.getProfile().coins)
        // And its 200 XP are granted too, not silently swallowed.
        assertEquals(200, progress.getProfile().totalXp)
        assertEquals(5, shop.getPurchases().size)
    }

    @Test
    fun `equipping requires ownership`() = runTest {
        val equip = EquipRewardUseCase(shop, DirectTransactionRunner)

        assertEquals(EquipResult.NotOwned, equip(partyHat))

        setCoins(200)
        useCase(partyHat)
        assertEquals(EquipResult.Equipped, equip(partyHat))
        assertEquals(EquipResult.Removed, equip(partyHat))
    }
}
