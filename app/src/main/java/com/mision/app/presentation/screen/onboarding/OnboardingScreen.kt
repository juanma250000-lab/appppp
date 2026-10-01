package com.mision.app.presentation.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.Pet
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.MisionBackdrop
import com.mision.app.presentation.components.PagerDot
import com.mision.app.presentation.components.glassBorderBrush
import com.mision.app.presentation.components.glassFillBrush
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.OnboardingState
import com.mision.app.presentation.viewmodel.OnboardingStep
import com.mision.app.presentation.viewmodel.OnboardingViewModel

/**
 * First-run experience: three presentation screens plus name, pet, categories
 * and the notification permission. Everything is in Spanish and finishes with
 * "Comencemos.", which persists the whole setup.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.factory(
            progressRepository = container.progressRepository,
            petRepository = container.petRepository,
            settingsRepository = container.settingsRepository,
            setPreferredCategories = container.useCases.setPreferredCategories,
        ),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val keyboard = LocalSoftwareKeyboardController.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onPermissionRequested()
    }

    // The permission is asked once, right when its step becomes visible.
    LaunchedEffect(state.step) {
        if (state.step == OnboardingStep.PERMISSIONS &&
            !state.permissionRequested &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MisionBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = Dimens.ScreenHorizontalPadding),
        ) {
            OnboardingTopBar(
                onSkip = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.finish(onDone = onFinished)
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = Dimens.SpaceMd),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        (fadeIn(tween(Dimens.AnimMedium)) +
                            slideInVertically(animationSpec = tween(Dimens.AnimMedium)) { it / 6 }) togetherWith
                            (fadeOut(tween(Dimens.AnimFast)) +
                                slideOutVertically(animationSpec = tween(Dimens.AnimFast)) { -it / 8 })
                    },
                    label = "onboardingStep",
                ) { step ->
                    StepContent(
                        step = step,
                        state = state,
                        onUserName = viewModel::onUserName,
                        onPetName = viewModel::onPetName,
                        onToggleCategory = viewModel::toggleCategory,
                    )
                }
            }

            OnboardingFooter(
                step = state.step,
                canContinue = state.canContinue,
                isFinishing = state.isFinishing,
                onBack = viewModel::back,
                onNext = {
                    keyboard?.hide()
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.next()
                },
                onFinish = {
                    keyboard?.hide()
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.finish(onDone = onFinished)
                },
                modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}

/** Brand mark on the left, "Saltar" on the right. */
@Composable
private fun OnboardingTopBar(onSkip: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.SpaceMd, bottom = Dimens.SpaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "✨ Misión",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Saltar",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.RadiusXs))
                .clickable(role = Role.Button, onClick = onSkip)
                .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        )
    }
}

/** Renders the body of the current step. */
@Composable
private fun StepContent(
    step: OnboardingStep,
    state: OnboardingState,
    onUserName: (String) -> Unit,
    onPetName: (String) -> Unit,
    onToggleCategory: (MissionCategory) -> Unit,
) {
    when (step) {
        OnboardingStep.INTRO_ONE -> IntroStep(
            emoji = "🎯",
            title = "Tu día, paso a paso",
            message = "Misión convierte tus objetivos en retos diarios pequeños, claros y posibles de completar.",
            bullets = listOf(
                "Misiones adaptadas a tus categorías favoritas",
                "Dificultad y recompensa visibles antes de empezar",
                "Filtros y buscador para no perderte en nada",
            ),
        )

        OnboardingStep.INTRO_TWO -> IntroStep(
            emoji = "🏆",
            title = "Progresa de verdad",
            message = "Cada misión completada te da experiencia y monedas. La constancia se nota: tu racha crece cada día.",
            bullets = listOf(
                "Niveles con curva de experiencia progresiva",
                "Rachas con hitos de 3, 7, 14, 30, 60, 100 y 365 días",
                "Logros, estadísticas semanales y mensuales",
            ),
        )

        OnboardingStep.INTRO_THREE -> IntroStep(
            emoji = "🐾",
            title = "Una mascota que te acompaña",
            message = "Tu mascota vive de tu actividad: si completas misiones, estará feliz y crecerá contigo.",
            bullets = listOf(
                "Humor, felicidad y energía en tiempo real",
                "Tienda de recompensas sin dinero real",
                "Personaliza sombreros, colores y efectos",
            ),
        )

        OnboardingStep.YOUR_NAME -> InputStep(
            emoji = "👋",
            title = "¿Cómo te llamas?",
            message = "Así te vamos a saludar cada mañana.",
            value = state.userName,
            onValueChange = onUserName,
            placeholder = "Tu nombre",
            helper = "Puedes cambiarlo luego desde tu perfil.",
        )

        OnboardingStep.PET_NAME -> PetNameStep(
            state = state,
            onPetName = onPetName,
        )

        OnboardingStep.CATEGORIES -> CategoriesStep(
            selected = state.categories,
            onToggle = onToggleCategory,
        )

        OnboardingStep.PERMISSIONS -> IntroStep(
            emoji = "🔔",
            title = "Recordatorios útiles",
            message = "Te avisaremos solo cuando haga falta: cuando quede poco por completar el día o alcances un hito. Sin ruido y sin publicidad.",
            bullets = listOf(
                "Un único aviso al día, a la hora que elijas",
                "Celebraciones de rachas y niveles",
                "Puedes desactivarlo cuando quieras en Ajustes",
            ),
        )
    }
}

/** Big emoji, title, message and a short bullet list. */
@Composable
private fun IntroStep(
    emoji: String,
    title: String,
    message: String,
    bullets: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
        Box(
            modifier = Modifier
                .size(Dimens.IconXxl * 1.8f)
                .clip(RoundedCornerShape(Dimens.RadiusXl))
                .background(Brush.linearGradient(AppGradients.primary)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, style = MaterialTheme.typography.displayMedium)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            bullets.forEach { bullet ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "✅", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(Dimens.SpaceSm))
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/** Title, message and a single line text field (used for the user name). */
@Composable
private fun InputStep(
    emoji: String,
    title: String,
    message: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    helper: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
        Text(text = emoji, style = MaterialTheme.typography.displayMedium)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OnboardingTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
        )
        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Pet name step with a live preview of the companion. */
@Composable
private fun PetNameStep(
    state: OnboardingState,
    onPetName: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    val preview = Pet.default(
        name = state.petName.ifBlank { "Mascota" },
        epochDay = container.clock.todayEpochDay(),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
    ) {
        AnimatedPet(
            pet = preview,
            size = 190.dp,
            accessibilityLabel = "Vista previa de tu mascota",
        )
        Text(
            text = "Ponle nombre a tu mascota",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Te acompañará cada día y reaccionará a lo que consigas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OnboardingTextField(
            value = state.petName,
            onValueChange = onPetName,
            placeholder = "Ej.: Nube",
        )
    }
}

/** Multi-select category grid. */
@Composable
private fun CategoriesStep(
    selected: Set<MissionCategory>,
    onToggle: (MissionCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
        Text(
            text = "Elige tus categorías",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Con ellas personalizamos las misiones de cada día. Puedes cambiarlas luego en Ajustes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            MissionCategory.entries.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    rowItems.forEach { category ->
                        CategoryToggle(
                            category = category,
                            selected = category in selected,
                            onClick = { onToggle(category) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        if (selected.isEmpty()) {
            Text(
                text = "Selecciona al menos una categoría para continuar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Selectable pill that signals its state with colour and border. */
@Composable
private fun CategoryToggle(
    category: MissionCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.RadiusPill)
    Row(
        modifier = modifier
            .height(Dimens.ButtonHeightCompact)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary,
                        ),
                    )
                } else {
                    glassFillBrush()
                },
                shape,
            )
            .border(
                width = if (selected) 0.dp else Dimens.GlassBorder,
                brush = glassBorderBrush(),
                shape = shape,
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = Dimens.SpaceMd),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = category.emoji, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(Dimens.SpaceXs))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/** Shared text field styled like the rest of the glass system. */
@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(text = placeholder) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        shape = RoundedCornerShape(Dimens.RadiusSm),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
    )
}

/** Step dots, back button and the primary action, including "Comencemos.". */
@Composable
private fun OnboardingFooter(
    step: OnboardingStep,
    canContinue: Boolean,
    isFinishing: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFirst = step == OnboardingStep.INTRO_ONE
    val isLast = step == OnboardingStep.PERMISSIONS

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.SpaceLg, bottom = Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                PagerDot(active = index == step.dotIndex)
                if (index < 2) Spacer(Modifier.width(Dimens.SpaceSm))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            if (!isFirst) {
                GlassButton(
                    text = "Atrás",
                    onClick = onBack,
                    style = GlassButtonStyle.TONAL,
                    modifier = Modifier.weight(1f),
                )
            }
            GlassButton(
                text = when {
                    isFinishing -> "Un momento…"
                    isLast -> "Comencemos."
                    else -> "Continuar"
                },
                onClick = if (isLast) onFinish else onNext,
                enabled = canContinue && !isFinishing,
                modifier = Modifier.weight(if (isFirst) 1f else 1.6f),
            )
        }
    }
}
