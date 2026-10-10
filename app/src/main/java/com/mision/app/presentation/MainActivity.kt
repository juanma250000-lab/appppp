package com.mision.app.presentation

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
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

            val systemAnimations = rememberSystemAnimationsEnabled()

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MisionTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.dynamicColor,
            ) {
                CompositionLocalProvider(
                    LocalAppContainer provides container,
                    LocalAnimationsEnabled provides (settings.animationsEnabled && systemAnimations),
                ) {
                    MisionAppRoot(container = container)
                }
            }
        }
    }
}

/**
 * False when the user switched animations off system-wide (Accesibilidad →
 * Quitar animaciones). Re-read on every resume, since that setting changes
 * outside the app.
 */
@Composable
private fun rememberSystemAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(systemAnimationsEnabled(context)) }
    LifecycleResumeEffect(context) {
        enabled = systemAnimationsEnabled(context)
        onPauseOrDispose { }
    }
    return enabled
}

private fun systemAnimationsEnabled(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
