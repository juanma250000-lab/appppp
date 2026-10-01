package com.mision.app.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Brand palette for Misión.
 *
 * A single violet brand colour carries the identity; teal and rose are quiet
 * supporting accents. Surfaces are neutral so content, not decoration, leads.
 * Every foreground/background pair below meets WCAG AA for normal text.
 */

/** Brand violet, shared by the launcher icon, the splash and the in-app mark. */
val BrandViolet = Color(0xFF5B47F0)
val BrandVioletLight = Color(0xFF7B68FF)

internal val LightColors = lightColorScheme(
    primary = BrandViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E1FF),
    onPrimaryContainer = Color(0xFF1C0F5C),
    secondary = Color(0xFF0F7F76),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCF3EE),
    onSecondaryContainer = Color(0xFF002A26),
    tertiary = Color(0xFFC63A63),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3F0018),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF1A1A24),
    surface = Color(0xFFFCFBFF),
    onSurface = Color(0xFF1A1A24),
    surfaceVariant = Color(0xFFE4E3EC),
    onSurfaceVariant = Color(0xFF55546A),
    surfaceTint = BrandViolet,
    surfaceBright = Color(0xFFFCFBFF),
    surfaceDim = Color(0xFFDCDBE4),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F5FB),
    surfaceContainer = Color(0xFFF0EFF7),
    surfaceContainerHigh = Color(0xFFEAE9F2),
    surfaceContainerHighest = Color(0xFFE4E3EC),
    outline = Color(0xFF7A798E),
    outlineVariant = Color(0xFFD9D8E3),
    error = Color(0xFFC4323A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD7),
    onErrorContainer = Color(0xFF410004),
    inverseSurface = Color(0xFF2F2E38),
    inverseOnSurface = Color(0xFFF2F0FA),
    inversePrimary = Color(0xFFBCB2FF),
    scrim = Color.Black,
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFBCB2FF),
    onPrimary = Color(0xFF26168A),
    primaryContainer = Color(0xFF3F2DC2),
    onPrimaryContainer = Color(0xFFE6E1FF),
    secondary = Color(0xFF6FDACD),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005049),
    onSecondaryContainer = Color(0xFFA2F2E7),
    tertiary = Color(0xFFFFB1C3),
    onTertiary = Color(0xFF650030),
    tertiaryContainer = Color(0xFF8C1A45),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = Color(0xFF111018),
    onBackground = Color(0xFFE6E4EF),
    surface = Color(0xFF111018),
    onSurface = Color(0xFFE6E4EF),
    surfaceVariant = Color(0xFF2E2D38),
    onSurfaceVariant = Color(0xFFC6C4D3),
    surfaceTint = Color(0xFFBCB2FF),
    surfaceBright = Color(0xFF37353F),
    surfaceDim = Color(0xFF111018),
    surfaceContainerLowest = Color(0xFF0C0B12),
    surfaceContainerLow = Color(0xFF18171F),
    surfaceContainer = Color(0xFF1D1C25),
    surfaceContainerHigh = Color(0xFF27262F),
    surfaceContainerHighest = Color(0xFF32313A),
    outline = Color(0xFF908E9E),
    outlineVariant = Color(0xFF3B3A45),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE6E4EF),
    inverseOnSurface = Color(0xFF2F2E38),
    inversePrimary = BrandViolet,
    scrim = Color.Black,
)

/**
 * Game specific roles Material does not cover. They are theme aware so the
 * coin, the streak flame and success states keep their contrast in dark mode.
 */
@Immutable
data class GameColors(
    val coin: Color,
    val onCoin: Color,
    val streak: Color,
    val streakContainer: Color,
    val success: Color,
    val onSuccess: Color,
)

internal val LightGameColors = GameColors(
    coin = Color(0xFFF5B82E),
    onCoin = Color(0xFF3D2A00),
    streak = Color(0xFFC2410C),
    streakContainer = Color(0xFFFFE8D9),
    success = Color(0xFF1B8A4E),
    onSuccess = Color.White,
)

internal val DarkGameColors = GameColors(
    coin = Color(0xFFF5C451),
    onCoin = Color(0xFF3D2A00),
    streak = Color(0xFFFF9A5C),
    streakContainer = Color(0xFF4A2A14),
    success = Color(0xFF4ADE80),
    onSuccess = Color(0xFF00391C),
)

/** Confetti palette for celebrations; decorative only, never carries meaning. */
val ConfettiColors: List<Color> = listOf(
    BrandVioletLight,
    Color(0xFF2EC4B6),
    Color(0xFFF5B82E),
    Color(0xFFFF6F91),
)
