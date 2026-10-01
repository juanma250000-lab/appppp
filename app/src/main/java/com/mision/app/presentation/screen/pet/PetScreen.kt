package com.mision.app.presentation.screen.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.usecase.PetAction
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.HeroSurface
import com.mision.app.presentation.components.InfoChip
import com.mision.app.presentation.components.LabeledStatBar
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionCard
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.brandTonalButtonColors
import com.mision.app.presentation.components.OnResumeEffect
import com.mision.app.presentation.components.PetSpeechBubble
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.SnackbarMessageEffect
import com.mision.app.presentation.components.XpIndicator
import com.mision.app.presentation.components.icon
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.PetViewModel

/**
 * Mascota: the pet stage, its mood and stats, the daily care actions and the
 * cosmetics it is wearing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetScreen(onOpenShop: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: PetViewModel = viewModel(
        factory = PetViewModel.factory(
            useCases = container.useCases,
            petRepository = container.petRepository,
            clock = container.clock,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.messages.current.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pet = state.pet

    OnResumeEffect(viewModel::refreshMood)
    SnackbarMessageEffect(message, snackbar, viewModel.messages::consumed)

    MisionScreen(
        title = pet.name,
        subtitle = "Estado de ánimo: ${pet.mood.displayName.lowercase()}",
        snackbarHostState = snackbar,
    ) {
        HeroSurface(modifier = Modifier.fillMaxWidth()) {
            AnimatedPet(pet = pet, size = Dimens.PetStage)
            PetSpeechBubble(text = state.speech, petName = pet.name)
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Estado")
            Column(
                modifier = Modifier.padding(top = Dimens.SpaceMd),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                LabeledStatBar(
                    icon = Icons.Filled.Favorite,
                    label = "Felicidad",
                    value = pet.happiness,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                LabeledStatBar(
                    icon = Icons.Filled.Bolt,
                    label = "Energía",
                    value = pet.energy,
                    color = MaterialTheme.colorScheme.secondary,
                )
                XpIndicator(
                    level = pet.level,
                    progress = pet.levelProgress.progress,
                    xpIntoLevel = pet.levelProgress.xpIntoLevel,
                    xpToNext = pet.levelProgress.xpToNext,
                )
            }
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Cuídalo")
            Text(
                text = "Alimentar recupera energía, jugar sube la felicidad y descansar recarga pilas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.SpaceMd),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                PetAction.entries.forEach { action ->
                    PetActionButton(
                        action = action,
                        enabled = !busy,
                        onClick = { viewModel.interact(action) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Aspecto")
            if (state.wearing.isEmpty()) {
                Text(
                    text = "${pet.name} todavía no lleva nada especial. Consigue sombreros, colores y efectos en la tienda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    modifier = Modifier.padding(top = Dimens.SpaceSm),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    state.wearing.forEach { item ->
                        InfoChip(text = item.name, icon = item.category.icon)
                    }
                }
            }
            MisionButton(
                text = "Ir a la tienda",
                onClick = onOpenShop,
                style = MisionButtonStyle.OUTLINE,
                leadingIcon = Icons.Filled.Storefront,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.SpaceMd),
            )
        }
    }
}

/** Care action: icon over label, so the three fit side by side on any phone. */
@Composable
private fun PetActionButton(
    action: PetAction,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = Dimens.PetActionHeight),
        shape = MaterialTheme.shapes.medium,
        colors = brandTonalButtonColors(),
        contentPadding = PaddingValues(Dimens.SpaceXs),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(action.icon, contentDescription = null, modifier = Modifier.size(Dimens.IconLg))
            Text(
                text = action.displayName,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val PetAction.icon
    get() = when (this) {
        PetAction.ALIMENTAR -> Icons.Filled.Restaurant
        PetAction.JUGAR -> Icons.Filled.SportsEsports
        PetAction.DESCANSAR -> Icons.Filled.Bedtime
    }
