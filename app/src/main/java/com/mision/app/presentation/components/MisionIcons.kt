package com.mision.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.mision.app.domain.model.AchievementIcon
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.MissionCategory

/**
 * Custom glyphs drawn on the Material 24dp grid, so they sit next to the
 * Material icons with the same weight. They are tinted like any other icon.
 */
object MisionIcons {

    /** Coin with a sparkle: the in-game currency. */
    val Coin: ImageVector by lazy {
        icon("Coin") {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                // Outer rim.
                moveTo(2f, 12f)
                arcToRelative(10f, 10f, 0f, true, true, 20f, 0f)
                arcToRelative(10f, 10f, 0f, true, true, -20f, 0f)
                close()
                // Inner face (cut out).
                moveTo(4.6f, 12f)
                arcToRelative(7.4f, 7.4f, 0f, true, true, 14.8f, 0f)
                arcToRelative(7.4f, 7.4f, 0f, true, true, -14.8f, 0f)
                close()
                // Sparkle, filled again by the even-odd rule.
                moveTo(12f, 7f)
                lineTo(13.4f, 10.6f)
                lineTo(17f, 12f)
                lineTo(13.4f, 13.4f)
                lineTo(12f, 17f)
                lineTo(10.6f, 13.4f)
                lineTo(7f, 12f)
                lineTo(10.6f, 10.6f)
                close()
            }
        }
    }

    /** Party hat: the "Sombreros" category of the shop. */
    val PartyHat: ImageVector by lazy {
        icon("PartyHat") {
            path(fill = SolidColor(Color.Black)) {
                // Pompom.
                moveTo(9.8f, 3.4f)
                arcToRelative(2.2f, 2.2f, 0f, true, true, 4.4f, 0f)
                arcToRelative(2.2f, 2.2f, 0f, true, true, -4.4f, 0f)
                close()
                // Cone.
                moveTo(12f, 5.6f)
                lineTo(18.2f, 18f)
                lineTo(5.8f, 18f)
                close()
                // Brim.
                moveTo(4.5f, 18.5f)
                horizontalLineTo(19.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 0f, 3f)
                horizontalLineTo(4.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 0f, -3f)
                close()
            }
        }
    }

    /** Round glasses: the "Accesorios" category of the shop. */
    val Glasses: ImageVector by lazy {
        icon("Glasses") {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                lens(centerX = 6.5f)
                lens(centerX = 17.5f)
            }
            path(fill = SolidColor(Color.Black)) {
                // Bridge.
                moveTo(10.2f, 11.6f)
                quadToRelative(1.8f, -1.6f, 3.6f, 0f)
                verticalLineToRelative(1.6f)
                quadToRelative(-1.8f, -1.2f, -3.6f, 0f)
                close()
            }
        }
    }

    private fun androidx.compose.ui.graphics.vector.PathBuilder.lens(centerX: Float) {
        val outer = 4.3f
        val inner = 2.6f
        moveTo(centerX - outer, 13f)
        arcToRelative(outer, outer, 0f, true, true, outer * 2, 0f)
        arcToRelative(outer, outer, 0f, true, true, -outer * 2, 0f)
        close()
        moveTo(centerX - inner, 13f)
        arcToRelative(inner, inner, 0f, true, true, inner * 2, 0f)
        arcToRelative(inner, inner, 0f, true, true, -inner * 2, 0f)
        close()
    }

    private fun icon(name: String, block: ImageVector.Builder.() -> ImageVector.Builder): ImageVector =
        ImageVector.Builder(
            name = "Mision.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).block().build()
}

/** One icon per mission category, used by chips, cards and filters. */
val MissionCategory.icon: ImageVector
    get() = when (this) {
        MissionCategory.SALUD -> Icons.Filled.WaterDrop
        MissionCategory.ESTUDIO -> Icons.AutoMirrored.Filled.MenuBook
        MissionCategory.PRODUCTIVIDAD -> Icons.Filled.Bolt
        MissionCategory.BIENESTAR -> Icons.Filled.SelfImprovement
        MissionCategory.ACTIVIDAD_FISICA -> Icons.AutoMirrored.Filled.DirectionsRun
        MissionCategory.PERSONAL -> Icons.Filled.Star
    }

/** One icon per shop category. */
val CosmeticSlot.icon: ImageVector
    get() = when (this) {
        CosmeticSlot.HAT -> MisionIcons.PartyHat
        CosmeticSlot.ACCESSORY -> MisionIcons.Glasses
        CosmeticSlot.BACKGROUND -> Icons.Filled.Landscape
        CosmeticSlot.COLOR -> Icons.Filled.Palette
        CosmeticSlot.EFFECT -> Icons.Filled.AutoAwesome
        CosmeticSlot.EMOTE -> Icons.Filled.EmojiEmotions
        CosmeticSlot.SKIN -> Icons.Filled.Texture
    }

/** Maps the catalogue icon family to a glyph. */
val AchievementIcon.icon: ImageVector
    get() = when (this) {
        AchievementIcon.TROPHY -> Icons.Filled.EmojiEvents
        AchievementIcon.FLAME -> Icons.Filled.LocalFireDepartment
        AchievementIcon.STAR -> Icons.Filled.Star
        AchievementIcon.CHECK -> Icons.Filled.CheckCircle
        AchievementIcon.BOOK -> Icons.AutoMirrored.Filled.MenuBook
        AchievementIcon.HEART -> Icons.Filled.Favorite
        AchievementIcon.BOLT -> Icons.Filled.Bolt
        AchievementIcon.MEDAL -> Icons.Filled.WorkspacePremium
        AchievementIcon.CROWN -> Icons.Filled.MilitaryTech
        AchievementIcon.SPARKLE -> Icons.Filled.AutoAwesome
    }
