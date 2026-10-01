package com.mision.app.presentation.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.Pet
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.BrandMark
import com.mision.app.presentation.components.IconBadge
import com.mision.app.presentation.components.MisionBackground
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionFilterChip
import com.mision.app.presentation.components.icon
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.OnboardingState
import com.mision.app.presentation.viewmodel.OnboardingStep
import com.mision.app.presentation.viewmodel.OnboardingViewModel

/**
 * First-run experience: three presentation screens plus name, pet, categories
 * and the notification permission. "Comenzar" persists the whole setup.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.factory(container.useCases.completeOnboarding),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val keyboard = LocalSoftwareKeyboardController.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { viewModel.onPermissionRequested() }

    // The permission is asked once, right when its step becomes visible.
    LaunchedEffect(state.step) {
        if (state.step == OnboardingStep.PERMISSIONS &&
            !state.permissionRequested &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val goNext: () -> Unit = {
        keyboard?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        viewModel.next()
    }
    val finish: () -> Unit = {
        keyboard?.hide()
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        viewModel.finish(onDone = onFinished)
    }

    MisionBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = Dimens.ContentMaxWidth)
                    .weight(1f)
                    .padding(horizontal = Dimens.ScreenHorizontalPadding),
            ) {
                OnboardingTopBar(state = state, onSkip = finish)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = Dimens.SpaceXl),
                ) {
                    AnimatedContent(
                        targetState = state.step,
                        transitionSpec = { fadeIn(tween(Dimens.AnimMedium)) togetherWith fadeOut(tween(Dimens.AnimFast)) },
                        label = "onboardingStep",
                    ) { step ->
                        StepContent(
                            step = step,
                            state = state,
                            onUserName = viewModel::onUserName,
                            onPetName = viewModel::onPetName,
                            onToggleCategory = viewModel::toggleCategory,
                            onDone = { if (state.canContinue) goNext() },
                        )
                    }
                }
                OnboardingFooter(
                    state = state,
                    onBack = viewModel::back,
                    onNext = goNext,
                    onFinish = finish,
                )
            }
        }
    }
}

/** Logo and progress on top, "Saltar" on the right. */
@Composable
private fun OnboardingTopBar(state: OnboardingState, onSkip: () -> Unit) {
    val steps = OnboardingStep.entries.size
    Column(
        modifier = Modifier.padding(top = Dimens.SpaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark()
            Text(
                text = "Misión",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = Dimens.SpaceSm)
                    .weight(1f),
            )
            if (!state.isLastStep) {
                TextButton(onClick = onSkip, enabled = !state.isFinishing) { Text("Saltar") }
            }
        }
        LinearProgressIndicator(
            progress = { (state.step.ordinal + 1f) / steps },
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepContent(
    step: OnboardingStep,
    state: OnboardingState,
    onUserName: (String) -> Unit,
    onPetName: (String) -> Unit,
    onToggleCategory: (MissionCategory) -> Unit,
    onDone: () -> Unit,
) {
    when (step) {
        OnboardingStep.INTRO_ONE -> IntroStep(
            icon = Icons.Filled.Flag,
            title = "Tu día, paso a paso",
            message = "Misión convierte tus objetivos en retos diarios pequeños, claros y posibles de completar.",
            bullets = listOf(
                "Tus categorías favoritas, siempre primero",
                "Dificultad y recompensa visibles antes de empezar",
                "Filtros y buscador para no perderte en nada",
            ),
        )
        OnboardingStep.INTRO_TWO -> IntroStep(
            icon = Icons.Filled.EmojiEvents,
            title = "Progresa de verdad",
            message = "Cada misión completada te da experiencia y monedas. La constancia se nota: tu racha crece cada día.",
            bullets = listOf(
                "Niveles con curva de experiencia progresiva",
                "Rachas con hitos de 3, 7, 14, 30, 60, 100 y 365 días",
                "Logros y estadísticas de la semana y del mes",
            ),
        )
        OnboardingStep.INTRO_THREE -> IntroStep(
            icon = Icons.Filled.Pets,
            title = "Una mascota que te acompaña",
            message = "Tu mascota vive de tu actividad: si completas misiones, estará feliz y crecerá contigo.",
            bullets = listOf(
                "Felicidad, energía y estado de ánimo en tiempo real",
                "Tienda de recompensas sin dinero real",
                "Sombreros, colores, fondos y efectos para personalizarla",
            ),
        )
        OnboardingStep.YOUR_NAME -> InputStep(
            icon = Icons.Filled.Person,
            title = "¿Cómo te llamas?",
            message = "Así te vamos a saludar cada día. Puedes cambiarlo luego desde tu perfil.",
            value = state.userName,
            onValueChange = onUserName,
            placeholder = "Tu nombre",
            onDone = onDone,
        )
        OnboardingStep.PET_NAME -> PetNameStep(state = state, onPetName = onPetName, onDone = onDone)
        OnboardingStep.CATEGORIES -> CategoriesStep(selected = state.categories, onToggle = onToggleCategory)
        OnboardingStep.PERMISSIONS -> IntroStep(
            icon = Icons.Filled.NotificationsActive,
            title = "Un recordatorio útil",
            message = "Te avisaremos una vez al día para que no pierdas tu racha. Sin ruido y sin publicidad.",
            bullets = listOf(
                "Un único aviso al día, a la hora que elijas",
                "Te dice cuántas misiones te quedan",
                "Puedes desactivarlo cuando quieras en Ajustes",
            ),
        )
    }
}

@Composable
private fun StepHeader(icon: ImageVector, title: String, message: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
        IconBadge(icon = icon, size = Dimens.BrandMarkLarge)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IntroStep(icon: ImageVector, title: String, message: String, bullets: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2xl)) {
        StepHeader(icon = icon, title = title, message = message)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            bullets.forEach { bullet ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconMd),
                    )
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

@Composable
private fun InputStep(
    icon: ImageVector,
    title: String,
    message: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2xl)) {
        StepHeader(icon = icon, title = title, message = message)
        NameField(value = value, onValueChange = onValueChange, placeholder = placeholder, onDone = onDone)
    }
}

/** Pet name step with a live preview of the companion. */
@Composable
private fun PetNameStep(state: OnboardingState, onPetName: (String) -> Unit, onDone: () -> Unit) {
    val preview = remember { Pet.default(epochDay = 0) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
    ) {
        AnimatedPet(pet = preview, accessibilityLabel = "Vista previa de tu mascota")
        Text(
            text = "Ponle nombre a tu mascota",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = "Te acompañará cada día y reaccionará a lo que consigas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        NameField(value = state.petName, onValueChange = onPetName, placeholder = "Ej.: ${Pet.DEFAULT_NAME}", onDone = onDone)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesStep(selected: Set<MissionCategory>, onToggle: (MissionCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2xl)) {
        StepHeader(
            icon = Icons.Filled.Flag,
            title = "Elige tus categorías",
            message = "Sus misiones aparecerán primero en tu lista del día. Puedes cambiarlas luego en Ajustes.",
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            MissionCategory.entries.forEach { category ->
                MisionFilterChip(
                    label = category.displayName,
                    icon = category.icon,
                    selected = category in selected,
                    onClick = { onToggle(category) },
                )
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

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit, placeholder: String, onDone: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}

/** Back and the primary action ("Continuar", or "Comenzar" on the last step). */
@Composable
private fun OnboardingFooter(
    state: OnboardingState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpaceLg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        if (!state.isFirstStep) {
            MisionButton(
                text = "Atrás",
                onClick = onBack,
                style = MisionButtonStyle.OUTLINE,
                enabled = !state.isFinishing,
                modifier = Modifier.weight(1f),
            )
        }
        MisionButton(
            text = when {
                state.isFinishing -> "Un momento…"
                state.isLastStep -> "Comenzar"
                else -> "Continuar"
            },
            onClick = if (state.isLastStep) onFinish else onNext,
            enabled = state.canContinue && !state.isFinishing,
            modifier = Modifier.weight(if (state.isFirstStep) 1f else PRIMARY_WEIGHT),
        )
    }
}

private const val PRIMARY_WEIGHT = 1.6f
