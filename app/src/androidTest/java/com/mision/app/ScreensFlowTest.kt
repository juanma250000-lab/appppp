package com.mision.app

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.screen.missions.MissionsScreen
import com.mision.app.presentation.screen.settings.SettingsScreen
import com.mision.app.presentation.theme.MisionTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Missions and settings: the two flows with side effects on user data. */
@RunWith(AndroidJUnit4::class)
class ScreensFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val container: AppContainer =
        (ApplicationProvider.getApplicationContext<Application>() as MisionApp).container

    private fun setScreen(content: @androidx.compose.runtime.Composable () -> Unit) {
        composeRule.setContent {
            MisionTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    content()
                }
            }
        }
    }

    @Test
    fun missionsScreen_showsTheDayAndOpensTheEditor() {
        setScreen { MissionsScreen() }

        composeRule.onNodeWithText("Misiones").assertIsDisplayed()
        composeRule.onNodeWithText("Progreso de hoy").assertIsDisplayed()
        composeRule.onNodeWithText("Buscar misiones").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Crear una misión").performClick()
        composeRule.onNodeWithText("Nueva misión").assertIsDisplayed()
        composeRule.onNodeWithText("Nombre de la misión").assertIsDisplayed()
        composeRule.onNodeWithText("Dificultad (define la recompensa)").assertIsDisplayed()

        composeRule.onNodeWithText("Cerrar").performClick()
        composeRule.onNodeWithText("Nueva misión").assertDoesNotExist()
    }

    @Test
    fun settingsScreen_resetAsksForConfirmationInSpanish() {
        setScreen { SettingsScreen(onBack = { /* not asserted here */ }) }

        composeRule.onNodeWithText("Ajustes").assertIsDisplayed()
        composeRule.onNodeWithText("Apariencia").assertIsDisplayed()

        composeRule.onNodeWithText("Restablecer todo el progreso")
            .performScrollTo()
            .performClick()

        // Confirmation first: the destructive action never runs immediately.
        composeRule.onNodeWithText("¿Restablecer todo el progreso?").assertIsDisplayed()
        composeRule.onNodeWithText("Sí, restablecer").assertIsDisplayed()

        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.onNodeWithText("¿Restablecer todo el progreso?").assertDoesNotExist()
        composeRule.onNodeWithText("Restablecer todo el progreso").assertIsDisplayed()
    }
}
