package com.mision.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

private val MisionShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimens.RadiusXs),
    small = RoundedCornerShape(Dimens.RadiusSm),
    medium = RoundedCornerShape(Dimens.RadiusMd),
    large = RoundedCornerShape(Dimens.RadiusLg),
    extraLarge = RoundedCornerShape(Dimens.RadiusXl),
)

private val LocalGameColors = staticCompositionLocalOf { LightGameColors }

/** Whether decorative motion (pet idle loop, confetti) should play. */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/** Game specific colours (coins, streak, success) for the current theme. */
object MisionColors {
    val game: GameColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGameColors.current
}

/**
 * App theme.
 *
 * @param dynamicColor when true the system wallpaper palette is used for the
 * Material roles (Android 12+); game colours always keep their meaning.
 * @param animationsEnabled user setting that gates decorative animations.
 */
@Composable
fun MisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Material You palettes only exist from Android 12: older devices fall
    // back to the brand light/dark schemes instead of crashing.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(
        LocalGameColors provides if (darkTheme) DarkGameColors else LightGameColors,
        LocalAnimationsEnabled provides animationsEnabled,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MisionTypography,
            shapes = MisionShapes,
            content = content,
        )
    }
}

/** True for dark schemes (including dynamic ones), judged by the background. */
val ColorScheme.isDark: Boolean get() = background.luminance() < 0.5f
