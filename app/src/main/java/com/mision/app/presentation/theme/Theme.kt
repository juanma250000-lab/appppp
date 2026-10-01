package com.mision.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = VioletOnPrimary,
    primaryContainer = VioletContainer,
    onPrimaryContainer = OnVioletContainer,
    secondary = AquaSecondary,
    onSecondary = Color.White,
    secondaryContainer = AquaContainer,
    onSecondaryContainer = OnAquaContainer,
    tertiary = RoseTertiary,
    onTertiary = Color.White,
    tertiaryContainer = RoseContainer,
    onTertiaryContainer = OnRoseContainer,
    background = LightBackground,
    onBackground = OnLightSurface,
    surface = LightSurface,
    onSurface = OnLightSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
    outline = LightOutline,
    outlineVariant = Color(0xFFC6C7D6),
    error = DangerRed,
    onError = Color.White,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD8D9E6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F6FE),
    surfaceContainer = Color(0xFFF0F1FA),
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = Color(0xFFE4E6F3),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2F3040),
    inverseOnSurface = Color(0xFFF1F1FA),
    inversePrimary = Color(0xFFB3A8FF),
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF003839),
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF56002D),
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = OnDarkSurface,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF3A3860),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    surfaceBright = Color(0xFF37364E),
    surfaceDim = Color(0xFF09081C),
    surfaceContainerLowest = Color(0xFF050517),
    surfaceContainerLow = DarkSurface,
    surfaceContainer = Color(0xFF161530),
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = Color(0xFF242244),
    scrim = Color.Black,
    inverseSurface = Color(0xFFECEBFF),
    inverseOnSurface = Color(0xFF1A1B25),
    inversePrimary = VioletPrimary,
)

private val MisionShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusXs),
    small = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusSm),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusMd),
    large = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusXl),
)

/**
 * App theme.
 *
 * @param dynamicColor when true the system wallpaper palette is used for the
 * Material roles. Glass surfaces always derive from the current [MaterialTheme]
 * colors so both modes stay coherent.
 */
@Composable
fun MisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Material You palettes only exist from Android 12: older devices fall
    // back to the built-in light/dark schemes instead of crashing.
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && supportsDynamic && darkTheme ->
            dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && supportsDynamic -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MisionTypography,
        shapes = MisionShapes,
        content = content,
    )
}
