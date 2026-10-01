package com.mision.app.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import com.mision.app.AppContainer

/**
 * Composition root accessor for the UI. Screens use it only to obtain
 * ViewModel factories; they never touch repositories directly.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no está inicializado")
}
