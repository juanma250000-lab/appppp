package com.mision.app

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.screen.onboarding.OnboardingScreen
import com.mision.app.presentation.theme.MisionTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First-run experience: the seven steps, their Spanish copy and the rules that
 * gate the "Continuar" button.
 */
@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val container: AppContainer =
        (ApplicationProvider.getApplicationContext<Application>() as MisionApp).container

    private fun launch() {
        composeRule.setContent {
            MisionTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    OnboardingScreen(onFinished = { /* not asserted here */ })
                }
            }
        }
    }

    @Test
    fun walkthrough_presentsEveryStepInSpanish() {
        launch()

        // --- Introduction ---------------------------------------------------
        composeRule.onNodeWithText("Tu día, paso a paso").assertIsDisplayed()
        composeRule.onNodeWithText("Saltar").assertIsDisplayed()

        composeRule.onNodeWithText("Continuar").performClick()
        composeRule.onNodeWithText("Progresa de verdad").assertIsDisplayed()

        composeRule.onNodeWithText("Continuar").performClick()
        composeRule.onNodeWithText("Una mascota que te acompaña").assertIsDisplayed()

        // --- Your name ------------------------------------------------------
        composeRule.onNodeWithText("Continuar").performClick()
        composeRule.onNodeWithText("¿Cómo te llamas?").assertIsDisplayed()

        // The button stays disabled until a name is provided.
        composeRule.onNodeWithText("Continuar").assertIsNotEnabled()
        composeRule.onNode(hasSetTextAction()).performTextInput("Ana")
        composeRule.onNodeWithText("Continuar").assertIsEnabled()
        composeRule.onNodeWithText("Continuar").performClick()

        // --- Pet name -------------------------------------------------------
        composeRule.onNodeWithText("Ponle nombre a tu mascota").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar").assertIsNotEnabled()
        composeRule.onNode(hasSetTextAction()).performTextInput("Nube")
        composeRule.onNodeWithText("Continuar").performClick()

        // --- Categories -----------------------------------------------------
        composeRule.onNodeWithText("Elige tus categorías").assertIsDisplayed()
        composeRule.onNodeWithText("Selecciona al menos una categoría para continuar.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Continuar").assertIsNotEnabled()

        composeRule.onNodeWithText("Salud").performClick()
        composeRule.onNodeWithText("Continuar").assertIsEnabled()
    }

    @Test
    fun goingBackKeepsTheInformationAlreadyGiven() {
        launch()

        repeat(3) { composeRule.onNodeWithText("Continuar").performClick() }

        composeRule.onNode(hasSetTextAction()).performTextInput("Ana")
        composeRule.onNodeWithText("Continuar").performClick()
        composeRule.onNodeWithText("Ponle nombre a tu mascota").assertIsDisplayed()

        composeRule.onNodeWithText("Atrás").performClick()
        composeRule.onNodeWithText("¿Cómo te llamas?").assertIsDisplayed()

        // The name typed before is still there, so the flow never loses data.
        composeRule.onNode(hasSetTextAction()).assert(hasText("Ana"))
    }
}
