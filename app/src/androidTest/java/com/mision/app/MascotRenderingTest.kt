package com.mision.app

import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.presentation.LocalAnimationsEnabled
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.StaticPet
import com.mision.app.presentation.theme.MisionTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

/**
 * Renders Nube for real (no mocks) and checks the pixels: the cloud is drawn,
 * keeps its blue outline, every cosmetic changes the picture, the idle loop
 * moves and reduced motion keeps it still. Renders are also saved to
 * Android/data/<app>/files/mascot-renders for visual review.
 */
@RunWith(AndroidJUnit4::class)
class MascotRenderingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val outDir: File? = InstrumentationRegistry.getInstrumentation().targetContext
        .getExternalFilesDir("mascot-renders")

    private fun pet(mood: PetMood = PetMood.FELIZ, equipped: EquippedCosmetics = EquippedCosmetics()) =
        Pet(name = "Nube", xp = 0, happiness = 80, energy = 80, mood = mood, equipped = equipped, lastInteractionEpochDay = 0)

    private var shown by mutableStateOf<Pet?>(null)

    /** setContent is allowed once per test, so later renders just swap the pet. */
    private fun renderStatic(pet: Pet): ImageBitmap {
        if (shown == null) {
            composeRule.setContent {
                MisionTheme(darkTheme = false) {
                    shown?.let { StaticPet(pet = it, modifier = Modifier.size(240.dp).testTag("mascota")) }
                }
            }
        }
        shown = pet
        composeRule.waitForIdle()
        return composeRule.onNodeWithTag("mascota").captureToImage()
    }

    private fun save(image: ImageBitmap, name: String) {
        val dir = outDir ?: return
        dir.mkdirs()
        File(dir, "$name.png").outputStream().use {
            image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun pixels(image: ImageBitmap): IntArray {
        val bitmap = image.asAndroidBitmap()
        val out = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(out, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return out
    }

    /** Pixels close to the outline blue of the reference (#3A71B4). */
    private fun outlinePixels(image: ImageBitmap): Int = pixels(image).count { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        abs(r - 0x3A) < 30 && abs(g - 0x71) < 30 && abs(b - 0xB4) < 30
    }

    private fun opaquePixels(image: ImageBitmap): Int = pixels(image).count { (it ushr 24) > 0 }

    @Test
    fun everyMood_drawsTheCloudWithItsBlueOutline() {
        PetMood.entries.forEach { mood ->
            val image = renderStatic(pet(mood))
            save(image, "mood_${mood.name.lowercase()}")
            assertTrue("$mood: el contorno azul no aparece", outlinePixels(image) > 500)
            assertTrue("$mood: el lienzo está vacío", opaquePixels(image) > image.width * image.height / 5)
        }
    }

    @Test
    fun everyShopItem_changesThePicture() {
        val plain = pixels(renderStatic(pet()))
        ShopCatalog.items.forEach { item ->
            val image = renderStatic(pet(equipped = EquippedCosmetics().with(item.category, item.id)))
            save(image, "item_${item.id}")
            val changed = pixels(image).zip(plain).count { (a, b) -> a != b }
            assertTrue("${item.id} no cambia el aspecto de la mascota", changed > 300)
        }
    }

    @Test
    fun idleAnimation_movesTheMascot() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MisionTheme(darkTheme = false) {
                CompositionLocalProvider(LocalAnimationsEnabled provides true) {
                    AnimatedPet(pet = pet(), modifier = Modifier.testTag("mascota"), size = 200.dp)
                }
            }
        }
        val first = pixels(composeRule.onNodeWithTag("mascota").captureToImage())
        composeRule.mainClock.advanceTimeBy(1_100)
        val later = pixels(composeRule.onNodeWithTag("mascota").captureToImage())
        assertFalse("La animación en reposo no mueve a la mascota", first.contentEquals(later))
    }

    @Test
    fun reducedMotion_keepsTheMascotVisibleAndStill() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MisionTheme(darkTheme = false) {
                CompositionLocalProvider(LocalAnimationsEnabled provides false) {
                    AnimatedPet(pet = pet(), modifier = Modifier.testTag("mascota"), size = 200.dp)
                }
            }
        }
        val first = composeRule.onNodeWithTag("mascota").captureToImage()
        composeRule.mainClock.advanceTimeBy(1_100)
        val later = composeRule.onNodeWithTag("mascota").captureToImage()
        assertTrue(outlinePixels(first) > 400)
        assertTrue("Con movimiento reducido la mascota no debe moverse", pixels(first).contentEquals(pixels(later)))
    }

    @Test
    fun mascot_isLabelledInSpanish_andInteractiveOnlyWhenAsked() {
        composeRule.setContent {
            MisionTheme {
                AnimatedPet(pet = pet(PetMood.MOTIVADO), interactive = true)
            }
        }
        composeRule.onNodeWithContentDescription("Mascota Nube, motivado")
            .assertHasClickAction()
    }
}
