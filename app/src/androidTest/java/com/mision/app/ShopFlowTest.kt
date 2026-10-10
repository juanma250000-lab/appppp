package com.mision.app

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.screen.shop.ShopScreen
import com.mision.app.presentation.theme.MisionTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Shop on the real container. It only browses (filters, preview, close):
 * nothing is bought, so the device's saved progress is never touched. The
 * purchase rules themselves are covered by ShopViewModelTest.
 */
@RunWith(AndroidJUnit4::class)
class ShopFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val container: AppContainer =
        (ApplicationProvider.getApplicationContext<Application>() as MisionApp).container

    private fun setShop() {
        composeRule.setContent {
            MisionTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    ShopScreen(onBack = { })
                }
            }
        }
    }

    @Test
    fun shop_showsTheMascotHeroAndFiltersByCategory() {
        setShop()

        composeRule.onNodeWithText("Tienda").assertIsDisplayed()
        composeRule.onNodeWithText("¿Qué me pruebo hoy?").assertIsDisplayed()
        composeRule.onNodeWithText("Colección").assertIsDisplayed()

        composeRule.onNodeWithText("Fondos").performClick()
        composeRule.onNodeWithText("Amanecer").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Corona dorada").assertDoesNotExist()

        composeRule.onNodeWithText("Todo").performClick()
        composeRule.onNodeWithText("Corona dorada").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun tappingAnItem_opensItsPreview_withoutSpendingCoins() {
        val coinsBefore = runBlocking { container.progressRepository.getProfile().coins }
        setShop()

        composeRule.onNodeWithText("Sombreros").performClick()
        composeRule.onNodeWithText("Corona dorada").performScrollTo().performClick()

        composeRule.onNodeWithContentDescription("con Corona dorada", substring = true).assertIsDisplayed()
        // Title in the card behind plus the heading of the sheet.
        assertEquals(2, composeRule.onAllNodesWithText("Corona dorada").fetchSemanticsNodes().size)
        composeRule.onNodeWithText("Cerrar").performClick()
        composeRule.onNodeWithContentDescription("con Corona dorada", substring = true).assertDoesNotExist()

        val coinsAfter = runBlocking { container.progressRepository.getProfile().coins }
        assertEquals(coinsBefore, coinsAfter)
    }
}
