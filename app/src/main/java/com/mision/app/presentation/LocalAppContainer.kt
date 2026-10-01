package com.mision.app.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.mision.app.AppContainer

/**
 * Composition root accessor for the UI. Screens use it only to obtain
 * ViewModel factories; they never touch repositories directly.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no está inicializado")
}

/**
 * Extra space the floating bottom bar needs, so scrollable content is never
 * hidden behind it. It is 0 on routes that do not show the bar.
 */
val LocalNavBottomPadding = staticCompositionLocalOf { 0.dp }
