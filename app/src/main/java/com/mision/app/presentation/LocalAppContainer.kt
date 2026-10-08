package com.mision.app.presentation

import androidx.compose.runtime.compositionLocalOf
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

/**
 * Mirrors the "Animaciones" setting so decorative motion (pet idle loop,
 * backdrop drift) can be switched off everywhere without threading the flag
 * through every screen.
 */
val LocalAnimationsEnabled = compositionLocalOf { true }
