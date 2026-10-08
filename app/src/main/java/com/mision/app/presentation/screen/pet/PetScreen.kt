package com.mision.app.presentation.screen.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.LabeledStatBar
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.PetSpeechBubble
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.TintedGlassSurface
import com.mision.app.presentation.components.XpIndicator
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.PetViewModel
import com.mision.app.domain.usecase.PetAction

/**
 * Mascota: the pet stage, its mood and stats, the daily interactions and the
 * cosmetics it is wearing.
 */
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

    LaunchedEffect(Unit) { viewModel.refreshMood() }

    state.infoMessage?.let { message ->
        ConfirmDialog(
            title = state.pet.name,
            message = message,
            confirmLabel = "Vale",
            onConfirm = viewModel::dismissInfo,
            onDismiss = viewModel::dismissInfo,
            dismissLabel = "Cerrar",
        )
    }

    MisionScreen(
        title = state.pet.name,
        subtitle = state.pet.mood.displayName,
    ) {
        TintedGlassSurface(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                AnimatedPet(pet = state.pet, size = 210.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = state.pet.mood.emoji, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = state.pet.mood.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                PetSpeechBubble(
                    text = state.speech,
                    petName = state.pet.name,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
                SectionHeader(title = "Estado")
                LabeledStatBar(
                    emoji = "💛",
                    label = "Felicidad",
                    value = state.pet.happiness,
                    colors = AppGradients.primary,
                )
                LabeledStatBar(
                    emoji = "⚡",
                    label = "Energía",
                    value = state.pet.energy,
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF9BE15D),
                        androidx.compose.ui.graphics.Color(0xFF4CC9F0),
                    ),
                )
                XpIndicator(
                    level = state.pet.level,
                    progress = state.pet.levelProgress.progress,
                    xpIntoLevel = state.pet.levelProgress.xpIntoLevel,
                    xpToNext = state.pet.levelProgress.xpToNext,
                )
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Cuídalo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    GlassButton(
                        text = PetAction.ALIMENTAR.displayName,
                        onClick = { viewModel.interact(PetAction.ALIMENTAR) },
                        style = GlassButtonStyle.TONAL,
                        modifier = Modifier.weight(1f),
                        height = Dimens.ButtonHeightCompact,
                        enabled = !state.busy,
                        contentPadding = PaddingValues(horizontal = Dimens.SpaceSm),
                    )
                    GlassButton(
                        text = PetAction.JUGAR.displayName,
                        onClick = { viewModel.interact(PetAction.JUGAR) },
                        modifier = Modifier.weight(1f),
                        height = Dimens.ButtonHeightCompact,
                        enabled = !state.busy,
                        contentPadding = PaddingValues(horizontal = Dimens.SpaceSm),
                    )
                    GlassButton(
                        text = PetAction.DESCANSAR.displayName,
                        onClick = { viewModel.interact(PetAction.DESCANSAR) },
                        style = GlassButtonStyle.TONAL,
                        modifier = Modifier.weight(1f),
                        height = Dimens.ButtonHeightCompact,
                        enabled = !state.busy,
                        contentPadding = PaddingValues(horizontal = Dimens.SpaceSm),
                    )
                }
                Text(
                    text = "Alimentar recupera energía, jugar sube la felicidad y descansar recarga pilas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Aspecto")
                val equipped = equippedDescriptions(state.pet.equipped)
                if (equipped.isEmpty()) {
                    Text(
                        text = "${state.pet.name} no lleva nada especial todavía.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    equipped.forEach { description ->
                        Text(
                            text = "• $description",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                GlassButton(
                    text = "Ir a la tienda",
                    onClick = onOpenShop,
                    style = GlassButtonStyle.TONAL,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun equippedDescriptions(
    equipped: com.mision.app.domain.model.EquippedCosmetics,
): List<String> = buildList {
    equipped.hat?.let { ShopCatalog.byId(it) }?.let { add("Sombrero: ${it.name}") }
    equipped.accessory?.let { ShopCatalog.byId(it) }?.let { add("Accesorio: ${it.name}") }
    equipped.color?.let { ShopCatalog.byId(it) }?.let { add("Color: ${it.name}") }
    equipped.background?.let { ShopCatalog.byId(it) }?.let { add("Fondo: ${it.name}") }
    equipped.effect?.let { ShopCatalog.byId(it) }?.let { add("Efecto: ${it.name}") }
    equipped.emote?.let { ShopCatalog.byId(it) }?.let { add("Emote: ${it.name}") }
    equipped.skin?.let { ShopCatalog.byId(it) }?.let { add("Piel: ${it.name}") }
}
