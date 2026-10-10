package com.mision.app.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Nube, the mascot, rebuilt as vector layers from the supplied illustration
 * (docs/mascota/nube-referencia.jpeg).
 *
 * Every coordinate below is in the reference image's own pixel space
 * (1200 × 1200): the silhouette is a union of circles fitted to the drawing
 * (IoU 0.994) and the eyes, mouth and cheeks sit where the artist put them.
 * The whole stage is then scaled to whatever size the caller asks for, so the
 * character stays sharp from a 40 dp thumbnail to a tablet hero.
 *
 * Depth is faked the 2.5D way: a back rim layer, a body layer and a face
 * layer that slide against each other while the cloud floats, plus a contact
 * shadow that shrinks as it rises.
 */
internal object CloudGeometry {
    /** Visible stage around the cloud (room for hats above and the shadow below). */
    const val STAGE_LEFT = -100f
    const val STAGE_TOP = -230f
    const val STAGE_SIZE = 1400f

    const val CENTER_X = 600f
    const val CENTER_Y = 570f
    const val BOTTOM = 968f
    const val OUTLINE = 18f

    /** x, y, radius of each puff. */
    val lobes: List<Triple<Float, Float, Float>> = listOf(
        Triple(627f, 422f, 245f), // top
        Triple(323f, 480f, 161f), // top left
        Triple(905f, 504f, 144f), // top right
        Triple(271f, 680f, 211f), // left
        Triple(939f, 683f, 203f), // right
        Triple(482f, 749f, 220f), // bottom left
        Triple(757f, 710f, 241f), // bottom right
    )

    /** Core ellipse that fills the gaps between the puffs. */
    val core = Rect(332f, 178f, 870f, 728f)

    val leftEye = Offset(427f, 569f)
    val rightEye = Offset(750f, 570f)
    const val EYE_WIDTH = 130f
    const val EYE_HEIGHT = 141f
    val leftCheek = Offset(323f, 655f)
    val rightCheek = Offset(850f, 655f)
    const val CHEEK_RADIUS = 62f

    /** Puffs inset by [inset] and moved by [dy]; used for the outline and the inner layers. */
    fun silhouette(inset: Float, dy: Float = 0f): Path {
        var result = Path().apply {
            addOval(Rect(core.left + inset, core.top + inset + dy, core.right - inset, core.bottom - inset + dy))
        }
        lobes.forEach { (x, y, r) ->
            val puff = Path().apply { addOval(Rect(Offset(x, y + dy), r - inset)) }
            result = Path().apply { op(result, puff, PathOperation.Union) }
        }
        return result
    }
}

/**
 * Geometry is constant in reference space, so every path is built once and
 * shared by all mascots on screen (drawing only happens on the main thread).
 */
private object CloudPaths {
    val outer: Path by lazy { CloudGeometry.silhouette(0f) }
    val body: Path by lazy { CloudGeometry.silhouette(CloudGeometry.OUTLINE) }
    /** Lighter inner body, raised so a pale blue rim shows along the bottom of every puff. */
    val inner: Path by lazy { CloudGeometry.silhouette(CloudGeometry.OUTLINE + 30f, dy = -22f) }

    val sceneDisc: Path by lazy { Path().apply { addOval(Rect(Offset(600f, 550f), 620f)) } }

    val smile: Path by lazy {
        Path().apply {
            moveTo(542f, 603f)
            cubicTo(548f, 662f, 630f, 662f, 636f, 603f)
        }
    }
    val bigSmile: Path by lazy {
        Path().apply {
            moveTo(532f, 598f)
            cubicTo(540f, 672f, 638f, 672f, 646f, 598f)
        }
    }
    val openMouth: Path by lazy {
        Path().apply {
            moveTo(538f, 598f)
            lineTo(640f, 598f)
            cubicTo(638f, 676f, 540f, 676f, 538f, 598f)
            close()
        }
    }
    val frown: Path by lazy {
        Path().apply {
            moveTo(548f, 646f)
            cubicTo(558f, 604f, 620f, 604f, 630f, 646f)
        }
    }

    val partyHat: Path by lazy {
        Path().apply {
            moveTo(515f, 245f)
            quadraticTo(640f, 275f, 745f, 235f)
            lineTo(668f, -140f)
            close()
        }
    }
    val wizardHat: Path by lazy {
        Path().apply {
            moveTo(528f, 232f)
            cubicTo(580f, 110f, 640f, -40f, 700f, -130f)
            quadraticTo(752f, -170f, 790f, -118f)
            cubicTo(735f, -108f, 705f, 40f, 735f, 232f)
            close()
        }
    }
    val crown: Path by lazy {
        Path().apply {
            moveTo(520f, 252f)
            lineTo(505f, 105f)
            lineTo(575f, 172f)
            lineTo(630f, 62f)
            lineTo(685f, 172f)
            lineTo(755f, 105f)
            lineTo(740f, 252f)
            quadraticTo(630f, 272f, 520f, 252f)
            close()
        }
    }
    val scarfBand: Path by lazy {
        Path().apply {
            moveTo(120f, 760f)
            cubicTo(380f, 850f, 820f, 850f, 1090f, 760f)
            lineTo(1095f, 858f)
            cubicTo(820f, 945f, 380f, 945f, 115f, 858f)
            close()
        }
    }
    val scarfTail: Path by lazy {
        Path().apply {
            moveTo(790f, 850f)
            lineTo(880f, 862f)
            lineTo(905f, 1018f)
            lineTo(815f, 1008f)
            close()
        }
    }
    val miniPuff: Path by lazy {
        val a = Path().apply { addOval(Rect(Offset(1185f, 470f), 52f)) }
        val b = Path().apply { addOval(Rect(Offset(1150f, 515f), 44f)) }
        val c = Path().apply { addOval(Rect(Offset(1225f, 520f), 40f)) }
        val ab = Path().apply { op(a, b, PathOperation.Union) }
        Path().apply { op(ab, c, PathOperation.Union) }
    }

    /** Unit four-point star centred on the origin. */
    val star: Path by lazy {
        Path().apply {
            moveTo(0f, -1f)
            quadraticTo(0.18f, -0.18f, 1f, 0f)
            quadraticTo(0.18f, 0.18f, 0f, 1f)
            quadraticTo(-0.18f, 0.18f, -1f, 0f)
            quadraticTo(-0.18f, -0.18f, 0f, -1f)
            close()
        }
    }

    /** Unit heart centred on the origin. */
    val heart: Path by lazy {
        Path().apply {
            moveTo(0f, 0.7f)
            cubicTo(-1.4f, -0.2f, -0.75f, -1.25f, 0f, -0.55f)
            cubicTo(0.75f, -1.25f, 1.4f, -0.2f, 0f, 0.7f)
            close()
        }
    }

    /** Unit raindrop (tip up). */
    val drop: Path by lazy {
        Path().apply {
            moveTo(0f, -1.3f)
            quadraticTo(0.8f, 0f, 0f, 0.6f)
            quadraticTo(-0.8f, 0f, 0f, -1.3f)
            close()
        }
    }

    /** Unit music note (head at the origin). */
    val note: Path by lazy {
        Path().apply {
            addOval(Rect(-0.55f, -0.38f, 0.45f, 0.38f))
            addRect(Rect(0.3f, -1.9f, 0.45f, 0f))
            moveTo(0.3f, -1.9f)
            quadraticTo(0.9f, -1.6f, 1.05f, -1.05f)
            quadraticTo(0.8f, -1.35f, 0.45f, -1.4f)
            close()
        }
    }
}

private val EyeInk = Color(0xFF0B0B14)
private val EyeReflection = Color(0xFF3A4785)
private val MouthInk = Color(0xFF2C1A18)
private val CheekPink = Color(0xFFFEA2AF)
private val CheekDeep = Color(0xFFFF8597)
private const val TWO_PI = (2 * PI).toFloat()

private fun styleOf(itemId: String?): String? = itemId?.let { ShopCatalog.byId(it)?.styleId }

/**
 * Draws Nube filling the receiver. [phase] is the idle cycle (0..2π), [blink]
 * the eyelid closure (0 open, 1 closed) and [hop] the progress of the tap /
 * success reaction (0..1). With [moving] false the pose is the neutral one.
 */
internal fun DrawScope.drawCloudMascot(
    pet: Pet,
    palette: MascotPalette,
    phase: Float = 0f,
    blink: Float = 0f,
    hop: Float = 0f,
    moving: Boolean = false,
) {
    val scale = size.minDimension / CloudGeometry.STAGE_SIZE
    val originX = (size.width - CloudGeometry.STAGE_SIZE * scale) / 2f - CloudGeometry.STAGE_LEFT * scale
    val originY = (size.height - CloudGeometry.STAGE_SIZE * scale) / 2f - CloudGeometry.STAGE_TOP * scale
    withTransform({
        translate(originX, originY)
        scale(scale, scale, Offset.Zero)
    }) {
        drawStage(pet, palette, phase, blink, hop, moving)
    }
}

private fun DrawScope.drawStage(
    pet: Pet,
    palette: MascotPalette,
    phase: Float,
    blink: Float,
    hop: Float,
    moving: Boolean,
) {
    val equipped = pet.equipped
    val emote = styleOf(equipped.emote)

    equipped.background?.let { id ->
        drawScene(styleOf(id), ShopCatalog.byId(id)?.previewColorHex)
    }

    val wave = if (moving) sin(phase) else 0f
    val hopArc = sin(PI.toFloat() * hop)
    val lift = -wave * 14f - hopArc * 90f
    val breathe = if (moving) sin(phase * 2f) else 0f
    val stretchY = 1f + breathe * 0.016f + hopArc * 0.06f
    val stretchX = 1f - breathe * 0.012f - hopArc * 0.04f
    val look = wave
    val rock = if (moving && emote == "dance") sin(phase * 2f) * 5f else 0f

    drawContactShadow(lift)
    if (pet.mood == PetMood.TRISTE) drawRain(phase, moving)

    withTransform({
        translate(0f, lift)
        rotate(look * 1.2f + rock, Offset(CloudGeometry.CENTER_X, CloudGeometry.CENTER_Y))
        scale(stretchX, stretchY, Offset(CloudGeometry.CENTER_X, CloudGeometry.BOTTOM))
    }) {
        drawBody(palette, look)
        if (styleOf(equipped.accessory) == "scarf") drawScarf()
        translate(look * 12f, 0f) {
            drawFace(pet.mood, blink)
            if (styleOf(equipped.accessory) == "glasses") drawGlasses()
        }
        translate(look * 6f, 0f) {
            styleOf(equipped.hat)?.let { drawHat(it) }
        }
        if (emote == "wave") drawWavingPuff(palette, phase, moving)
    }

    if (emote == "dance") drawNotes(phase, moving)
    when (styleOf(equipped.effect)) {
        "sparkle" -> drawSparkles(phase, moving)
        "hearts" -> drawHearts(phase, moving)
        null -> Unit
        else -> drawSparkles(phase, moving)
    }
}

// ---- Body ----------------------------------------------------------------

private fun DrawScope.drawContactShadow(lift: Float) {
    // Higher cloud → smaller, fainter shadow: the main depth cue.
    val k = (1f + lift / 600f).coerceIn(0.6f, 1.1f)
    val width = 760f * k
    val height = 84f * k
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1B2A4A).copy(alpha = 0.26f * k), Color.Transparent),
            center = Offset(CloudGeometry.CENTER_X, 1078f),
            radius = width / 2f,
        ),
        topLeft = Offset(CloudGeometry.CENTER_X - width / 2f, 1078f - height / 2f),
        size = Size(width, height),
    )
}

private fun DrawScope.drawBody(palette: MascotPalette, look: Float) {
    drawPath(CloudPaths.outer, palette.outline)
    drawPath(
        CloudPaths.body,
        brush = Brush.verticalGradient(
            listOf(palette.rimTop, palette.rimBottom),
            startY = 180f,
            endY = 960f,
        ),
    )
    clipPath(CloudPaths.body) {
        // The inner layer slides against the rim as the cloud turns.
        translate(-look * 7f, 0f) {
            val innerBrush = if (palette.finish == MascotFinish.AURORA) {
                Brush.horizontalGradient(
                    listOf(Color(0xFFC9F1FF), Color(0xFFE2D9FF), Color(0xFFFFD6EA)),
                    startX = 100f,
                    endX = 1100f,
                )
            } else {
                Brush.verticalGradient(
                    listOf(palette.bodyTop, palette.bodyTop, palette.bodyBottom),
                    startY = 200f,
                    endY = 900f,
                )
            }
            drawPath(CloudPaths.inner, brush = innerBrush)
        }
        when (palette.finish) {
            MascotFinish.GALAXY -> drawGalaxySpecks()
            MascotFinish.GOLD -> drawGoldSheen()
            else -> Unit
        }
        // Soft top light and the glossy streaks of the original drawing.
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(470f + look * 10f, 300f),
                radius = 190f,
            ),
            radius = 190f,
            center = Offset(470f + look * 10f, 300f),
        )
        rotate(-28f, Offset(118f, 705f)) {
            drawOval(Color.White.copy(alpha = 0.75f), Offset(98f, 650f), Size(40f, 118f))
        }
        drawOval(Color.White.copy(alpha = 0.55f), Offset(360f, 880f), Size(96f, 22f))
    }
}

private val galaxySpecks = listOf(
    Offset(300f, 420f) to 9f, Offset(850f, 470f) to 7f, Offset(180f, 700f) to 6f,
    Offset(1010f, 720f) to 9f, Offset(420f, 860f) to 7f, Offset(760f, 860f) to 10f,
    Offset(620f, 270f) to 6f, Offset(560f, 760f) to 5f, Offset(950f, 600f) to 5f,
)

private fun DrawScope.drawGalaxySpecks() {
    galaxySpecks.forEachIndexed { index, (center, radius) ->
        drawCircle(Color.White.copy(alpha = if (index % 2 == 0) 0.9f else 0.6f), radius, center)
    }
    drawStar(Offset(240f, 560f), 26f, Color(0xFFFFF3B0))
    drawStar(Offset(880f, 800f), 20f, Color(0xFFFFF3B0))
}

private fun DrawScope.drawGoldSheen() {
    drawArc(
        color = Color.White.copy(alpha = 0.6f),
        startAngle = 200f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(400f, 230f),
        size = Size(420f, 420f),
        style = Stroke(width = 22f, cap = StrokeCap.Round),
    )
    drawStar(Offset(330f, 420f), 30f, Color.White.copy(alpha = 0.9f))
}

// ---- Face ----------------------------------------------------------------

private fun DrawScope.drawFace(mood: PetMood, blink: Float) {
    drawCheek(CloudGeometry.leftCheek, mood)
    drawCheek(CloudGeometry.rightCheek, mood)

    if (mood == PetMood.ORGULLOSO) {
        drawHappyClosedEye(CloudGeometry.leftEye)
        drawHappyClosedEye(CloudGeometry.rightEye)
    } else {
        drawEye(CloudGeometry.leftEye, inward = 1f, blink = blink, tired = mood == PetMood.CANSADO)
        drawEye(CloudGeometry.rightEye, inward = -1f, blink = blink, tired = mood == PetMood.CANSADO)
    }

    val line = Stroke(width = 12f, cap = StrokeCap.Round)
    when (mood) {
        PetMood.FELIZ -> drawPath(CloudPaths.smile, MouthInk, style = line)
        PetMood.MOTIVADO -> {
            drawPath(CloudPaths.bigSmile, MouthInk, style = line)
            drawLine(MouthInk, Offset(380f, 462f), Offset(462f, 474f), 10f, StrokeCap.Round)
            drawLine(MouthInk, Offset(797f, 462f), Offset(715f, 474f), 10f, StrokeCap.Round)
        }
        PetMood.ORGULLOSO -> drawPath(CloudPaths.bigSmile, MouthInk, style = line)
        PetMood.CELEBRANDO -> {
            drawPath(CloudPaths.openMouth, MouthInk)
            clipPath(CloudPaths.openMouth) {
                drawCircle(Color(0xFFFF7A90), 34f, Offset(589f, 672f))
            }
        }
        PetMood.CANSADO -> drawLine(MouthInk, Offset(562f, 628f), Offset(616f, 628f), 11f, StrokeCap.Round)
        PetMood.TRISTE -> {
            drawPath(CloudPaths.frown, MouthInk, style = line)
            drawLine(MouthInk, Offset(385f, 490f), Offset(462f, 466f), 11f, StrokeCap.Round)
            drawLine(MouthInk, Offset(792f, 490f), Offset(715f, 466f), 11f, StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawEye(center: Offset, inward: Float, blink: Float, tired: Boolean) {
    val w = CloudGeometry.EYE_WIDTH
    val h = CloudGeometry.EYE_HEIGHT
    val open = 1f - blink.coerceIn(0f, 1f)
    if (open < 0.15f) {
        // Closed: a short lash line where the eye was.
        drawArc(
            color = EyeInk,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(center.x - w * 0.42f, center.y - 20f),
            size = Size(w * 0.84f, 48f),
            style = Stroke(width = 13f, cap = StrokeCap.Round),
        )
        return
    }
    val eyeH = h * open
    val top = center.y + h / 2f - eyeH - (h - eyeH) * 0.25f
    val eyeRect = Rect(center.x - w / 2f, top, center.x + w / 2f, top + eyeH)
    val eyePath = Path().apply { addOval(eyeRect) }
    // A tired eye is the same eye with its top half hidden under a heavy lid.
    val lidY = if (tired) eyeRect.top + eyeH * 0.48f else eyeRect.top - 1f
    val shineY = if (tired) lidY + eyeH * 0.14f else eyeRect.top + eyeH * 0.28f
    clipRect(left = eyeRect.left - 2f, top = lidY, right = eyeRect.right + 2f, bottom = eyeRect.bottom + 2f) {
        drawPath(eyePath, Brush.verticalGradient(listOf(Color(0xFF15152A), EyeInk), eyeRect.top, eyeRect.bottom))
        clipPath(eyePath) {
            // Navy reflection crescent along the bottom, as in the illustration.
            drawOval(EyeReflection, Offset(center.x - w * 0.36f, eyeRect.bottom - eyeH * 0.46f), Size(w * 0.72f, eyeH * 0.38f))
            drawOval(EyeInk, Offset(center.x - w * 0.36f, eyeRect.bottom - eyeH * 0.56f), Size(w * 0.72f, eyeH * 0.34f))
            drawCircle(Color(0xFF8A97D0), 8f, Offset(center.x - inward * w * 0.27f, eyeRect.bottom - eyeH * 0.24f))
            drawCircle(Color.White, 23f, Offset(center.x + inward * 13f, shineY))
            drawCircle(Color.White, 10f, Offset(center.x - inward * 24f, shineY - eyeH * 0.06f))
        }
    }
    if (tired) {
        drawLine(EyeInk, Offset(eyeRect.left + 4f, lidY), Offset(eyeRect.right - 4f, lidY), 11f, StrokeCap.Round)
    }
}

private fun DrawScope.drawHappyClosedEye(center: Offset) {
    drawArc(
        color = EyeInk,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - 52f, center.y - 22f),
        size = Size(104f, 84f),
        style = Stroke(width = 16f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawCheek(center: Offset, mood: PetMood) {
    val glow = if (mood == PetMood.CELEBRANDO || mood == PetMood.ORGULLOSO) 0.55f else 0.38f
    drawCircle(
        brush = Brush.radialGradient(listOf(CheekPink.copy(alpha = glow), Color.Transparent), center, 96f),
        radius = 96f,
        center = center,
    )
    val r = CloudGeometry.CHEEK_RADIUS
    drawCircle(Brush.radialGradient(listOf(CheekPink, CheekDeep), center, r), r, center)
    drawCircle(Color.White.copy(alpha = 0.85f), 14f, Offset(center.x - 18f, center.y - 22f))
    drawCircle(Color.White.copy(alpha = 0.85f), 7f, Offset(center.x + 22f, center.y - 15f))
}

// ---- Cosmetics -----------------------------------------------------------

private fun DrawScope.drawHat(styleId: String) {
    when (styleId) {
        "party" -> {
            drawPath(CloudPaths.partyHat, Color(0xFFFF7BAC))
            clipPath(CloudPaths.partyHat) {
                listOf(120f, 0f, -110f).forEach { y ->
                    drawLine(Color(0xFFFFE27A), Offset(480f, y + 60f), Offset(780f, y - 20f), 26f)
                }
            }
            drawPath(CloudPaths.partyHat, Color(0xFFB8306A), style = Stroke(10f, join = StrokeJoin.Round))
            drawCircle(Color(0xFFFFE27A), 40f, Offset(668f, -150f))
            drawCircle(Color(0xFFC9962A), 40f, Offset(668f, -150f), style = Stroke(8f))
        }
        "astro" -> {
            val brim = Rect(462f, 196f, 800f, 270f)
            drawOval(Color(0xFF4B3FB8), brim.topLeft, brim.size)
            drawPath(CloudPaths.wizardHat, Brush.verticalGradient(listOf(Color(0xFF3B2FA8), Color(0xFF5B4BE0)), -150f, 240f))
            drawPath(CloudPaths.wizardHat, Color(0xFF1E1760), style = Stroke(10f, join = StrokeJoin.Round))
            drawOval(Color(0xFF1E1760), brim.topLeft, brim.size, style = Stroke(10f))
            drawStar(Offset(612f, 130f), 30f, Color(0xFFFFE27A))
            drawStar(Offset(668f, 30f), 20f, Color(0xFFFFE27A))
            drawStar(Offset(690f, 180f), 14f, Color(0xFFFFF3B0))
        }
        "crown" -> {
            drawPath(CloudPaths.crown, Brush.verticalGradient(listOf(Color(0xFFFFE58A), Color(0xFFF5C542), Color(0xFFD9A21B)), 60f, 260f))
            drawPath(CloudPaths.crown, Color(0xFFA8770F), style = Stroke(10f, join = StrokeJoin.Round))
            listOf(Offset(505f, 105f), Offset(630f, 62f), Offset(755f, 105f)).forEach {
                drawCircle(Color(0xFFFFE58A), 20f, it)
                drawCircle(Color(0xFFA8770F), 20f, it, style = Stroke(7f))
            }
            drawCircle(Color(0xFFFF7BAC), 19f, Offset(570f, 218f))
            drawCircle(Color(0xFF4CC9F0), 23f, Offset(630f, 222f))
            drawCircle(Color(0xFFFF7BAC), 19f, Offset(690f, 218f))
        }
        else -> Unit
    }
}

private fun DrawScope.drawGlasses() {
    val frame = Color(0xFF22243A)
    listOf(CloudGeometry.leftEye, CloudGeometry.rightEye).forEach { eye ->
        drawCircle(Color(0xFF1B1D2E).copy(alpha = 0.55f), 94f, eye)
        drawArc(
            color = Color.White.copy(alpha = 0.55f),
            startAngle = 200f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = Offset(eye.x - 68f, eye.y - 68f),
            size = Size(136f, 136f),
            style = Stroke(width = 11f, cap = StrokeCap.Round),
        )
        drawCircle(frame, 94f, eye, style = Stroke(16f))
    }
    drawArc(
        color = frame,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(515f, 535f),
        size = Size(146f, 60f),
        style = Stroke(width = 14f, cap = StrokeCap.Round),
    )
    drawLine(frame, Offset(336f, 548f), Offset(246f, 522f), 13f, StrokeCap.Round)
    drawLine(frame, Offset(842f, 548f), Offset(932f, 522f), 13f, StrokeCap.Round)
}

private fun DrawScope.drawScarf() {
    val knit = Color(0xFFE5477E)
    val edge = Color(0xFF9E1F4C)
    clipPath(CloudPaths.body) {
        drawPath(CloudPaths.scarfBand, knit)
        clipPath(CloudPaths.scarfBand) {
            listOf(150f, 330f, 510f, 690f, 870f, 1050f).forEach { x ->
                drawLine(Color(0xFFFF8FB0), Offset(x, 740f), Offset(x + 40f, 960f), 22f)
            }
        }
        drawPath(CloudPaths.scarfBand, edge, style = Stroke(8f))
    }
    drawPath(CloudPaths.scarfTail, Color(0xFFD63A70))
    drawPath(CloudPaths.scarfTail, edge, style = Stroke(8f, join = StrokeJoin.Round))
    listOf(830f, 858f, 886f).forEach { x ->
        drawLine(edge, Offset(x, 1012f), Offset(x + 4f, 1046f), 8f, StrokeCap.Round)
    }
}

/** "Saludo": a cloud has no hands, so a little puff of itself waves instead. */
private fun DrawScope.drawWavingPuff(palette: MascotPalette, phase: Float, moving: Boolean) {
    val swing = if (moving) sin(phase * 3f) * 16f else -8f
    rotate(swing, Offset(1120f, 600f)) {
        drawPath(CloudPaths.miniPuff, palette.outline, style = Stroke(36f))
        drawPath(CloudPaths.miniPuff, Brush.verticalGradient(listOf(palette.bodyTop, palette.rimBottom), 420f, 560f))
    }
    val motion = Stroke(width = 10f, cap = StrokeCap.Round)
    listOf(70f, 110f).forEach { r ->
        drawArc(
            color = palette.outline.copy(alpha = 0.7f),
            startAngle = -70f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(1190f - r, 470f - r),
            size = Size(r * 2f, r * 2f),
            style = motion,
        )
    }
}

private fun DrawScope.drawNotes(phase: Float, moving: Boolean) {
    val lift = if (moving) sin(phase * 2f) * 18f else 0f
    drawUnitPath(CloudPaths.note, Offset(10f, 300f - lift), 62f, Color(0xFFFF7BAC), Color(0xFFB8306A))
    drawUnitPath(CloudPaths.note, Offset(1170f, 240f + lift), 54f, Color(0xFF9BE15D), Color(0xFF4E8A22))
    drawUnitPath(CloudPaths.note, Offset(1090f, 60f - lift), 42f, Color(0xFF4CC9F0), Color(0xFF1C7FA0))
}

private fun DrawScope.drawSparkles(phase: Float, moving: Boolean) {
    val turn = if (moving) phase / 2f else 0.4f
    for (i in 0 until 6) {
        val angle = turn + i * (TWO_PI / 6f)
        val center = Offset(600f + cos(angle) * 610f, 560f + sin(angle) * 470f)
        val pulse = if (moving) 1f + 0.25f * sin(phase * 2f + i) else 1f
        drawStar(center, (if (i % 2 == 0) 60f else 42f) * pulse, Color(0xFFFFE27A), Color(0xFFE0A82E))
    }
}

private fun DrawScope.drawHearts(phase: Float, moving: Boolean) {
    val xs = floatArrayOf(40f, 1150f, 170f, 1040f, 1210f)
    val cycle = if (moving) phase / TWO_PI else 0.35f
    xs.forEachIndexed { i, x ->
        val t = (cycle + i * 0.2f) % 1f
        val y = 920f - t * 900f
        val alpha = (if (t < 0.15f) t / 0.15f else if (t > 0.8f) (1f - t) / 0.2f else 1f).coerceIn(0f, 1f)
        drawUnitPath(
            CloudPaths.heart,
            Offset(x, y),
            if (i % 2 == 0) 62f else 46f,
            Color(0xFFFF7BAC).copy(alpha = alpha),
            Color(0xFFB8306A).copy(alpha = alpha),
        )
    }
}

/** Sad cloud: a light drizzle falls from under it. */
private fun DrawScope.drawRain(phase: Float, moving: Boolean) {
    val xs = floatArrayOf(400f, 600f, 800f, 500f, 700f)
    val cycle = if (moving) phase / TWO_PI else 0.3f
    xs.forEachIndexed { i, x ->
        val t = ((cycle * 2f) + i * 0.37f) % 1f
        val y = 990f + t * 170f
        val alpha = (1f - t) * 0.9f
        drawUnitPath(CloudPaths.drop, Offset(x, y), 30f, Color(0xFF6FA8F0).copy(alpha = alpha))
    }
}

// ---- Scenes (Fondos) -------------------------------------------------------

private fun DrawScope.drawScene(styleId: String?, previewHex: String?) {
    clipPath(CloudPaths.sceneDisc) {
        when (styleId) {
            "dawn" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFFF9E7A), Color(0xFFFFC48C), Color(0xFFFFE7C2)), -70f, 1170f), Offset(-20f, -70f), Size(1240f, 1240f))
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1B8), Color(0x00FFD36E)), Offset(600f, 1010f), 520f), 520f, Offset(600f, 1010f))
                drawCircle(Color(0xFFFFD36E), 240f, Offset(600f, 1010f))
                listOf(Offset(60f, 180f) to 260f, Offset(880f, 120f) to 220f, Offset(940f, 360f) to 170f).forEach { (o, w) ->
                    drawOval(Color(0xFFFFF4E6).copy(alpha = 0.75f), o, Size(w, 46f))
                }
            }
            "nebula" -> {
                drawRect(Brush.radialGradient(listOf(Color(0xFF5B3FD0), Color(0xFF2A1F7A), Color(0xFF120D3A)), Offset(600f, 480f), 760f), Offset(-20f, -70f), Size(1240f, 1240f))
                drawCircle(Brush.radialGradient(listOf(Color(0x66FF7BAC), Color.Transparent), Offset(260f, 240f), 320f), 320f, Offset(260f, 240f))
                drawCircle(Brush.radialGradient(listOf(Color(0x554CC9F0), Color.Transparent), Offset(980f, 860f), 320f), 320f, Offset(980f, 860f))
                nebulaStars.forEachIndexed { i, star ->
                    drawCircle(Color.White.copy(alpha = if (i % 3 == 0) 0.95f else 0.6f), if (i % 3 == 0) 7f else 4.5f, star)
                }
                drawStar(Offset(150f, 470f), 26f, Color(0xFFFFF3B0))
                drawStar(Offset(1060f, 180f), 22f, Color(0xFFFFF3B0))
            }
            "forest" -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFFD5F5E3), Color(0xFF9FE2BE)), -70f, 1170f), Offset(-20f, -70f), Size(1240f, 1240f))
                drawCircle(Color(0xFF55C98C), 560f, Offset(250f, 1290f))
                drawCircle(Color(0xFF3DB57A), 600f, Offset(1000f, 1330f))
                listOf(Offset(110f, 840f) to 1f, Offset(1090f, 820f) to 1.1f, Offset(220f, 930f) to 0.8f, Offset(990f, 940f) to 0.75f).forEach { (base, k) ->
                    drawRect(Color(0xFF7A5230), Offset(base.x - 12f * k, base.y - 10f), Size(24f * k, 60f * k))
                    val tree = Path().apply {
                        moveTo(base.x, base.y - 260f * k)
                        lineTo(base.x + 95f * k, base.y)
                        lineTo(base.x - 95f * k, base.y)
                        close()
                    }
                    drawPath(tree, Color(0xFF1E8A55))
                }
            }
            else -> {
                val halo = hexToColor(previewHex ?: "#4CC9F0", Color(0xFF4CC9F0))
                drawCircle(Brush.radialGradient(listOf(halo.copy(alpha = 0.55f), halo.copy(alpha = 0f)), Offset(600f, 550f), 620f), 620f, Offset(600f, 550f))
            }
        }
    }
    drawPath(CloudPaths.sceneDisc, Color.White.copy(alpha = 0.45f), style = Stroke(12f))
}

private val nebulaStars = listOf(
    Offset(120f, 200f), Offset(330f, 90f), Offset(520f, -10f), Offset(800f, 40f), Offset(1010f, 300f),
    Offset(1120f, 560f), Offset(80f, 700f), Offset(200f, 1000f), Offset(420f, 1110f), Offset(860f, 1080f),
    Offset(1060f, 980f), Offset(700f, 140f),
)

// ---- Helpers -------------------------------------------------------------

private fun DrawScope.drawStar(center: Offset, radius: Float, fill: Color, outline: Color? = null) {
    drawUnitPath(CloudPaths.star, center, radius, fill, outline)
}

private fun DrawScope.drawUnitPath(path: Path, center: Offset, scale: Float, fill: Color, outline: Color? = null) {
    withTransform({
        translate(center.x, center.y)
        scale(scale, scale, Offset.Zero)
    }) {
        drawPath(path, fill)
        if (outline != null) drawPath(path, outline, style = Stroke(width = 6f / scale, join = StrokeJoin.Round))
    }
}
