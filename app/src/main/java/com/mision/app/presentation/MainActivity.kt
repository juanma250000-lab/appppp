package com.mision.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mision.app.MisionApp
import com.mision.app.domain.model.AppSettings
import com.mision.app.domain.model.ThemeMode
import com.mision.app.presentation.navigation.MisionAppRoot
import com.mision.app.presentation.theme.MisionTheme

/**
 * Single activity. It only wires the theme, the dependency container and the
 * navigation graph; every screen is pure Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MisionApp).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MisionTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.dynamicColor,
            ) {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    MisionAppRoot(container = container)
                }
            }
        }
    }
}
