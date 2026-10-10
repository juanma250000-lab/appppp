package com.mision.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Brand palette for Misión.
 *
 * The identity comes from Nube, the cloud mascot: the sky blue of its outline
 * is the core colour, its pink cheeks the rose accent, with aqua as support. Light and dark variants are intentionally designed instead of being
 * an inversion of each other so the Liquid Glass surfaces keep their depth.
 */

// ---- Light palette -------------------------------------------------------
val VioletPrimary = Color(0xFF2F64C8)
val VioletOnPrimary = Color(0xFFFFFFFF)
val VioletContainer = Color(0xFFDCE7FF)
val OnVioletContainer = Color(0xFF0A2257)

val AquaSecondary = Color(0xFF00969A)
val AquaContainer = Color(0xFFCFF7F6)
val OnAquaContainer = Color(0xFF002021)

val RoseTertiary = Color(0xFFE5477E)
val RoseContainer = Color(0xFFFFDCE9)
val OnRoseContainer = Color(0xFF3E0021)

val LightBackground = Color(0xFFF2F6FF)
val LightSurface = Color(0xFFFAFCFF)
val LightSurfaceHigh = Color(0xFFE9EFFB)
val LightSurfaceVariant = Color(0xFFE0E7F4)
val OnLightSurface = Color(0xFF172033)
val OnLightSurfaceVariant = Color(0xFF434B5E)
val LightOutline = Color(0xFF727B8F)

// ---- Dark palette --------------------------------------------------------
val DarkPrimary = Color(0xFFA8C6FF)
val DarkOnPrimary = Color(0xFF0B2A66)
val DarkPrimaryContainer = Color(0xFF1F4A9A)
val DarkOnPrimaryContainer = Color(0xFFDCE7FF)

val DarkSecondary = Color(0xFF63E4E1)
val DarkSecondaryContainer = Color(0xFF005052)
val DarkOnSecondaryContainer = Color(0xFFCFF7F6)

val DarkTertiary = Color(0xFFFFB1C9)
val DarkTertiaryContainer = Color(0xFF8E1B4E)
val DarkOnTertiaryContainer = Color(0xFFFFDCE9)

val DarkBackground = Color(0xFF070D1F)
val DarkSurface = Color(0xFF0E1529)
val DarkSurfaceHigh = Color(0xFF162038)
val DarkSurfaceVariant = Color(0xFF1F2A47)
val OnDarkSurface = Color(0xFFE8EEFF)
val OnDarkSurfaceVariant = Color(0xFFB3BDD6)
val DarkOutline = Color(0xFF7F89A3)

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
    /** Full screen backdrop: a soft daytime sky with a blush of Nube's cheeks. */
    val backdropLight = listOf(
        Color(0xFFD9E8FF),
        Color(0xFFEFF5FF),
        Color(0xFFE6F3FF),
        Color(0xFFFFEFF3),
    )

    /** Night sky counterpart. */
    val backdropDark = listOf(
        Color(0xFF06112A),
        Color(0xFF0B1A3C),
        Color(0xFF08142F),
        Color(0xFF140F30),
    )

    /** Sky gradient for primary actions; white text keeps 4.5:1 on every stop. */
    val primary = listOf(Color(0xFF2E5DC0), Color(0xFF3468CC), Color(0xFF3A74D4))
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
