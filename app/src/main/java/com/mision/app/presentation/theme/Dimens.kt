package com.mision.app.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralised design tokens. Screens must never invent their own spacing,
 * radii or durations: they read them from here so the whole product stays
 * visually consistent and easy to re-skin.
 */
object Dimens {
    // Spacing scale
    val SpaceXs: Dp = 4.dp
    val SpaceSm: Dp = 8.dp
    val SpaceMd: Dp = 12.dp
    val SpaceLg: Dp = 16.dp
    val SpaceXl: Dp = 20.dp
    val Space2xl: Dp = 24.dp
    val Space3xl: Dp = 32.dp
    val Space4xl: Dp = 40.dp

    // Corner radius scale
    val RadiusXs: Dp = 10.dp
    val RadiusSm: Dp = 16.dp
    val RadiusMd: Dp = 22.dp
    val RadiusLg: Dp = 28.dp
    val RadiusXl: Dp = 36.dp
    val RadiusPill: Dp = 999.dp

    // Glass system
    val GlassBorder: Dp = 1.dp
    val GlassBlur: Dp = 22.dp
    val GlassBlurStrong: Dp = 34.dp
    val GlassElevation: Dp = 10.dp
    val GlassElevationHigh: Dp = 18.dp

    // Icons
    val IconSm: Dp = 16.dp
    val IconMd: Dp = 20.dp
    val IconLg: Dp = 24.dp
    val IconXl: Dp = 32.dp
    val IconXxl: Dp = 48.dp

    // Controls / touch targets (accessibility minimum is 48dp)
    val TouchTargetMin: Dp = 48.dp
    val ButtonHeight: Dp = 52.dp
    val ButtonHeightCompact: Dp = 40.dp
    val ProgressBarHeight: Dp = 12.dp
    val BottomBarHeight: Dp = 74.dp
    val ScreenHorizontalPadding: Dp = 20.dp

    // Layout widths for large screens (tablets, landscape)
    val ContentMaxWidth: Dp = 840.dp
    val BottomBarMaxWidth: Dp = 560.dp
    val DialogMaxWidth: Dp = 560.dp

    // Animation durations (ms)
    const val AnimFast = 160
    const val AnimMedium = 300
    const val AnimSlow = 520
    const val AnimCelebration = 950
    const val AnimPetIdle = 2200
}

/**
 * Glass surface tuning. The values are intentionally conservative: the app
 * should read as a premium translucent product, not as a foggy overlay.
 */
object GlassTokens {
    /** Fill opacity for light mode cards. */
    const val LightFillAlpha = 0.58f
    const val LightStrokeAlpha = 0.72f
    const val LightHighlightAlpha = 0.85f

    /** Fill opacity for dark mode cards. */
    const val DarkFillAlpha = 0.62f
    const val DarkStrokeAlpha = 0.16f
    const val DarkHighlightAlpha = 0.35f

    /** Content overlay that keeps text readable over gradients. */
    const val ScrimAlpha = 0.28f
}
