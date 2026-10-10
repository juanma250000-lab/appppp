package com.mision.app.presentation.viewmodel

import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.usecase.UseCases
import com.mision.app.testing.FakeClock
import com.mision.app.testing.FakeGamificationRepository
import com.mision.app.testing.FakeMissionRepository
import com.mision.app.testing.FakePetRepository
import com.mision.app.testing.FakeProgressRepository
import com.mision.app.testing.FakeSettingsRepository
import com.mision.app.testing.FakeShopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** The redesigned shop must keep the purchase rules exactly as they were. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val clock = FakeClock()
    private val progress = FakeProgressRepository()
    private val shop = FakeShopRepository()
    private val pets = FakePetRepository()
    private val gamification = FakeGamificationRepository(progress)
    private lateinit var viewModel: ShopViewModel

    private val partyHat = ShopCatalog.byId("hat_party")!!
    private val crown = ShopCatalog.byId("hat_crown")!!
    private val glasses = ShopCatalog.byId("acc_glasses")!!

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val useCases = UseCases(
            missionRepository = FakeMissionRepository(),
            progressRepository = progress,
            gamificationRepository = gamification,
            shopRepository = shop,
            petRepository = pets,
            settingsRepository = FakeSettingsRepository(),
            clock = clock,
        )
        viewModel = ShopViewModel(useCases, shop, progress, pets)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // uiState is WhileSubscribed: keep a subscriber so it stays up to date.
    private fun TestScope.collectState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private suspend fun setCoins(coins: Int) =
        progress.saveProfile(progress.getProfile().copy(coins = coins))

    @Test
    fun `the preview tries the item on over the current look`() = runTest {
        collectState()
        shop.seedPurchases(setOf("hat_party"))
        shop.equip("hat_party")

        viewModel.openPreview(glasses)
        val state = viewModel.uiState.value

        assertEquals(glasses, state.previewItem)
        val tried = state.tryOn(glasses).equipped
        assertEquals("acc_glasses", tried.accessory)
        assertEquals("hat_party", tried.hat)
        // Same slot: the new hat replaces the old one, as equipping would.
        assertEquals("hat_crown", state.tryOn(crown).equipped.hat)
        assertEquals("Nube", state.tryOn(crown).name)

        viewModel.closePreview()
        assertNull(viewModel.uiState.value.previewItem)
    }

    @Test
    fun `confirming in the preview buys the item and closes the sheet`() = runTest {
        collectState()
        setCoins(200)

        viewModel.openPreview(partyHat)
        viewModel.onItemAction(partyHat)

        val state = viewModel.uiState.value
        assertNull(state.previewItem)
        assertTrue(state.isOwned(partyHat))
        assertEquals(80, state.coins)
        assertTrue(viewModel.message.value!!.contains("comprado"))
        assertNull(viewModel.busyItemId.value)
    }

    @Test
    fun `without enough coins nothing is bought or charged`() = runTest {
        collectState()
        setCoins(50)

        viewModel.onItemAction(crown)

        val state = viewModel.uiState.value
        assertFalse(state.isOwned(crown))
        assertEquals(50, state.coins)
        assertEquals(
            "Te faltan 550 monedas. Completa misiones para conseguir más.",
            viewModel.message.value,
        )
    }

    @Test
    fun `an owned item toggles between equipped and removed without charging`() = runTest {
        collectState()
        setCoins(30)
        shop.seedPurchases(setOf("acc_glasses"))

        viewModel.onItemAction(glasses)
        assertTrue(viewModel.uiState.value.isEquipped(glasses))
        assertEquals("¡Gafas de sol equipado!", viewModel.message.value)

        viewModel.dismissMessage()
        viewModel.onItemAction(glasses)
        assertFalse(viewModel.uiState.value.isEquipped(glasses))
        assertEquals(30, viewModel.uiState.value.coins)
    }

    @Test
    fun `category filter still shows only that slot`() = runTest {
        collectState()

        viewModel.onSlotSelected(crown.category)

        val visible = viewModel.uiState.value.visibleItems
        assertTrue(visible.isNotEmpty())
        assertTrue(visible.all { it.category == crown.category })
        assertEquals(ShopCatalog.forSlot(crown.category).map { it.id }, visible.map { it.id })
    }
}
