package com.mision.app.presentation.components

import androidx.compose.ui.graphics.Color
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.PetMood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MascotPaletteTest {

    @Test
    fun `without cosmetics Nube keeps the colours of the reference drawing`() {
        val palette = mascotPaletteFor(EquippedCosmetics())

        assertEquals(MascotPalette.Default, palette)
        assertEquals(Color(0xFF3A71B4), palette.outline)
        assertEquals(Color.White, palette.bodyTop)
    }

    @Test
    fun `a colour item tints the body and darkens the outline`() {
        val palette = mascotPaletteFor(EquippedCosmetics(color = "color_mint"))

        assertNotEquals(MascotPalette.Default.bodyBottom, palette.bodyBottom)
        assertTrue(palette.outline.green < hexToColor("#7CF5C2", Color.Black).green)
        assertEquals(MascotFinish.PLAIN, palette.finish)
    }

    @Test
    fun `a colour item wins over a skin tint but the skin keeps its finish`() {
        val colourOnly = mascotPaletteFor(EquippedCosmetics(color = "color_coral"))
        val both = mascotPaletteFor(EquippedCosmetics(color = "color_coral", skin = "skin_gold"))

        assertEquals(colourOnly.bodyBottom, both.bodyBottom)
        assertEquals(MascotFinish.GOLD, both.finish)
    }

    @Test
    fun `galaxy skin turns Nube into a night cloud`() {
        val palette = mascotPaletteFor(EquippedCosmetics(skin = "skin_galaxy"))

        assertEquals(MascotFinish.GALAXY, palette.finish)
        assertTrue(palette.bodyTop.red < 0.6f)
    }

    @Test
    fun `aurora colour uses the aurora finish`() {
        assertEquals(
            MascotFinish.AURORA,
            mascotPaletteFor(EquippedCosmetics(color = "color_aurora")).finish,
        )
    }

    @Test
    fun `a sad cloud is greyer than a happy one`() {
        val happy = mascotPaletteFor(EquippedCosmetics(), PetMood.FELIZ)
        val sad = mascotPaletteFor(EquippedCosmetics(), PetMood.TRISTE)

        assertTrue(sad.bodyTop.blue < happy.bodyTop.blue)
        assertEquals(happy.outline, sad.outline)
    }

    @Test
    fun `unknown item ids fall back to the default look`() {
        assertEquals(
            MascotPalette.Default,
            mascotPaletteFor(EquippedCosmetics(color = "no_existe", skin = "tampoco")),
        )
    }

    @Test
    fun `hex colours parse with or without alpha and reject garbage`() {
        assertEquals(Color(0xFF3A71B4), hexToColor("#3A71B4", Color.Black))
        assertEquals(Color(0x803A71B4), hexToColor("#803A71B4", Color.Black))
        assertEquals(Color.Black, hexToColor("#XYZ", Color.Black))
        assertEquals(Color.Black, hexToColor("#12345", Color.Black))
    }

    @Test
    fun `blink only closes the eyes briefly at the end of the cycle`() {
        assertEquals(0f, blinkAmount(0f))
        assertEquals(0f, blinkAmount(0.5f))
        assertEquals(1f, blinkAmount(0.965f), 0.001f)
        assertTrue(blinkAmount(0.99f) < 0.5f)
    }
}
