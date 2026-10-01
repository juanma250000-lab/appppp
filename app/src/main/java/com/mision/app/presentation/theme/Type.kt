package com.mision.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Type scale. Only sizes/weights are defined here so the app inherits the
 * platform font (best rendering and full Spanish accent support) while keeping
 * a consistent rhythm. The smallest style is 12sp, never smaller.
 */
private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: TextUnit = 0.sp,
) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing,
)

val MisionTypography = Typography(
    displaySmall = style(34, 40, FontWeight.Bold, (-0.4).sp),
    headlineLarge = style(30, 36, FontWeight.Bold, (-0.3).sp),
    headlineMedium = style(26, 32, FontWeight.SemiBold, (-0.2).sp),
    headlineSmall = style(22, 28, FontWeight.SemiBold),
    titleLarge = style(20, 26, FontWeight.SemiBold),
    titleMedium = style(16, 22, FontWeight.SemiBold, 0.1.sp),
    titleSmall = style(14, 20, FontWeight.SemiBold, 0.1.sp),
    bodyLarge = style(16, 24, FontWeight.Normal, 0.15.sp),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.2.sp),
    bodySmall = style(12, 17, FontWeight.Normal, 0.3.sp),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1.sp),
    labelMedium = style(13, 18, FontWeight.Medium, 0.3.sp),
    labelSmall = style(12, 16, FontWeight.Medium, 0.4.sp),
)
