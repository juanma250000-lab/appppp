package com.mision.app.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralised design tokens. Screens must never invent their own spacing,
 * radii or durations: they read them from here so the whole product stays
 * visually consistent and easy to re-skin.
 */
object Dimens {
    // Spacing scale (4dp grid)
    val SpaceXs: Dp = 4.dp
    val SpaceSm: Dp = 8.dp
    val SpaceMd: Dp = 12.dp
    val SpaceLg: Dp = 16.dp
    val SpaceXl: Dp = 20.dp
    val Space2xl: Dp = 24.dp
    val Space3xl: Dp = 32.dp

    // Corner radius scale
    val RadiusXs: Dp = 8.dp
    val RadiusSm: Dp = 12.dp
    val RadiusMd: Dp = 16.dp
    val RadiusLg: Dp = 24.dp
    val RadiusXl: Dp = 28.dp

    // Strokes
    val Hairline: Dp = 1.dp

    // Icons
    val IconSm: Dp = 16.dp
    val IconMd: Dp = 20.dp
    val IconLg: Dp = 24.dp
    val IconContainer: Dp = 40.dp
    val IconContainerLg: Dp = 56.dp

    // Controls / touch targets (accessibility minimum is 48dp)
    val TouchTargetMin: Dp = 48.dp
    val ButtonHeight: Dp = 52.dp
    val ButtonHeightCompact: Dp = 44.dp
    val ProgressBarHeight: Dp = 8.dp
    val ScreenHorizontalPadding: Dp = 20.dp
    val ScreenBottomPadding: Dp = 32.dp
    val ContentMaxWidth: Dp = 640.dp
    val DialogMaxHeight: Dp = 680.dp

    // Illustrations
    val PetHero: Dp = 148.dp
    val PetStage: Dp = 200.dp
    val PetPreview: Dp = 112.dp
    val PetActionHeight: Dp = 72.dp
    val ShopPreviewHeight: Dp = 132.dp
    val WeeklyChartHeight: Dp = 96.dp
    val BrandMark: Dp = 32.dp
    val BrandMarkLarge: Dp = 72.dp

    // Animation durations (ms)
    const val AnimFast = 150
    const val AnimMedium = 280
    const val AnimConfetti = 2_400
    const val AnimPetIdle = 4_400
    const val AnimPetBlink = 3_400
}
