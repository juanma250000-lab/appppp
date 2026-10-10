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
    outlineVariant = Color(0xFFC3CBDA),
    error = DangerRed,
    onError = Color.White,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD5DCEA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F8FF),
    surfaceContainer = Color(0xFFEEF2FB),
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = Color(0xFFE2E8F4),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2B3346),
    inverseOnSurface = Color(0xFFEFF3FB),
    inversePrimary = Color(0xFFA8C6FF),
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
    outlineVariant = Color(0xFF34405E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    surfaceBright = Color(0xFF333D55),
    surfaceDim = Color(0xFF070D1F),
    surfaceContainerLowest = Color(0xFF050A18),
    surfaceContainerLow = DarkSurface,
    surfaceContainer = Color(0xFF121A30),
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = Color(0xFF212B45),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE8EEFF),
    inverseOnSurface = Color(0xFF172033),
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
