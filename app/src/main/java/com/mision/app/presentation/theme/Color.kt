package com.mision.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Brand palette for Misión.
 *
 * The identity is built around a deep violet/indigo core with aqua and rose
 * accents. Light and dark variants are intentionally designed instead of being
 * an inversion of each other so the Liquid Glass surfaces keep their depth.
 */

// ---- Light palette -------------------------------------------------------
val VioletPrimary = Color(0xFF5B4BE0)
val VioletOnPrimary = Color(0xFFFFFFFF)
val VioletContainer = Color(0xFFE5E1FF)
val OnVioletContainer = Color(0xFF180F52)

val AquaSecondary = Color(0xFF00969A)
val AquaContainer = Color(0xFFCFF7F6)
val OnAquaContainer = Color(0xFF002021)

val RoseTertiary = Color(0xFFE5477E)
val RoseContainer = Color(0xFFFFDCE9)
val OnRoseContainer = Color(0xFF3E0021)

val LightBackground = Color(0xFFF4F5FF)
val LightSurface = Color(0xFFFBFBFF)
val LightSurfaceHigh = Color(0xFFECEEFB)
val LightSurfaceVariant = Color(0xFFE3E4F2)
val OnLightSurface = Color(0xFF1A1B25)
val OnLightSurfaceVariant = Color(0xFF464754)
val LightOutline = Color(0xFF767786)

// ---- Dark palette --------------------------------------------------------
val DarkPrimary = Color(0xFFB3A8FF)
val DarkOnPrimary = Color(0xFF241A76)
val DarkPrimaryContainer = Color(0xFF3B2FA8)
val DarkOnPrimaryContainer = Color(0xFFE5E1FF)

val DarkSecondary = Color(0xFF63E4E1)
val DarkSecondaryContainer = Color(0xFF005052)
val DarkOnSecondaryContainer = Color(0xFFCFF7F6)

val DarkTertiary = Color(0xFFFFB1C9)
val DarkTertiaryContainer = Color(0xFF8E1B4E)
val DarkOnTertiaryContainer = Color(0xFFFFDCE9)

val DarkBackground = Color(0xFF09081C)
val DarkSurface = Color(0xFF111027)
val DarkSurfaceHigh = Color(0xFF191834)
val DarkSurfaceVariant = Color(0xFF232146)
val OnDarkSurface = Color(0xFFECEBFF)
val OnDarkSurfaceVariant = Color(0xFFB6B5D6)
val DarkOutline = Color(0xFF807F9F)

// ---- Accent / feedback colors -------------------------------------------
val SuccessGreen = Color(0xFF22B573)
val WarningAmber = Color(0xFFF5A524)
val DangerRed = Color(0xFFE5484D)
val CoinGold = Color(0xFFF5C542)
val StreakFlame = Color(0xFFFF7A45)
val XpLime = Color(0xFF9BE15D)
val XpBlue = Color(0xFF4CC9F0)

/**
 * Curated gradients used across the app. Keeping them in a single place makes
 * the visual identity easy to tune without touching screen code.
 */
object AppGradients {
    /** Full screen backdrop: layered, soft and slightly asymmetric. */
    val backdropLight = listOf(
        Color(0xFFE7E4FF),
        Color(0xFFF3F1FF),
        Color(0xFFEAF6FF),
        Color(0xFFFDEFF6),
    )

    val backdropDark = listOf(
        Color(0xFF0A0820),
        Color(0xFF140E33),
        Color(0xFF0A1030),
        Color(0xFF1A0E2A),
    )

    val primary = listOf(Color(0xFF6C5CE7), Color(0xFF8E7BFF), Color(0xFF4CC9F0))
    val xp = listOf(XpLime, XpBlue)
    val coin = listOf(Color(0xFFFFE27A), CoinGold, Color(0xFFE9A63A))
    val streak = listOf(Color(0xFFFFD166), StreakFlame, Color(0xFFF45B8A))
    val celebrate = listOf(Color(0xFFFFD166), Color(0xFF7CF5C2), Color(0xFF74A7FF), Color(0xFFE58BFF))
    val glassHighlight = listOf(
        Color.White.copy(alpha = 0.55f),
        Color.White.copy(alpha = 0.06f),
    )
    val glassHighlightDark = listOf(
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.03f),
    )
}

fun verticalGradient(colors: List<Color>) = Brush.verticalGradient(colors)
