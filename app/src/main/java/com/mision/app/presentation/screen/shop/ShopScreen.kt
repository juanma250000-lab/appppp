package com.mision.app.presentation.screen.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.ShopItem
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.CoinPill
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassChip
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.parseHexColor
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.ShopViewModel

/**
 * Tienda: cosmetics bought with coins earned in missions. There is no real
 * money anywhere in the app.
 */
@Composable
fun ShopScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ShopViewModel = viewModel(
        factory = ShopViewModel.factory(
            useCases = container.useCases,
            shopRepository = container.shopRepository,
            progressRepository = container.progressRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    message?.let { text ->
        ConfirmDialog(
            title = "Tienda",
            message = text,
            confirmLabel = "Vale",
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
            dismissLabel = "Cerrar",
        )
    }

    MisionScreen(
        title = "Tienda",
        subtitle = "Gana monedas cumpliendo misiones",
        onBack = onBack,
        actions = { CoinPill(coins = state.coins) },
    ) {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                SectionHeader(title = "Tu saldo")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    CoinPill(coins = state.coins)
                    Text(
                        text = "Las monedas se ganan completando misiones y manteniendo tu racha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            GlassChip(
                label = "Todo",
                selected = state.selectedSlot == null,
                onClick = { viewModel.onSlotSelected(null) },
            )
            CosmeticSlot.entries.take(4).forEach { slot ->
                GlassChip(
                    label = slot.displayName,
                    selected = state.selectedSlot == slot,
                    onClick = {
                        viewModel.onSlotSelected(if (state.selectedSlot == slot) null else slot)
                    },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            CosmeticSlot.entries.drop(4).forEach { slot ->
                GlassChip(
                    label = slot.displayName,
                    selected = state.selectedSlot == slot,
                    onClick = {
                        viewModel.onSlotSelected(if (state.selectedSlot == slot) null else slot)
                    },
                )
            }
        }

        val items = state.visibleItems
        if (items.isEmpty()) {
            GlassCard {
                Text(
                    text = "No hay artículos en esta categoría todavía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                    rowItems.forEach { item ->
                        RewardCard(
                            item = item,
                            owned = state.isOwned(item),
                            equipped = state.isEquipped(item),
                            affordable = state.canAfford(item),
                            missingCoins = (item.cost - state.coins).coerceAtLeast(0),
                            modifier = Modifier.weight(1f),
                            onAction = { viewModel.onItemAction(item) },
                        )
                    }
                    if (rowItems.size == 1) {
                        Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardCard(
    item: ShopItem,
    owned: Boolean,
    equipped: Boolean,
    affordable: Boolean,
    missingCoins: Int,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swatch = parseHexColor(item.previewColorHex, MaterialTheme.colorScheme.primary)
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                Box(
                    Modifier
                        .size(Dimens.IconLg)
                        .clip(CircleShape)
                        .background(swatch, CircleShape),
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            GlassButton(
                text = when {
                    equipped -> "Quitar"
                    owned -> "Equipar"
                    affordable -> "${item.cost} monedas"
                    else -> "Faltan $missingCoins monedas"
                },
                onClick = onAction,
                style = when {
                    equipped -> GlassButtonStyle.TONAL
                    owned -> GlassButtonStyle.PRIMARY
                    affordable -> GlassButtonStyle.PRIMARY
                    else -> GlassButtonStyle.TONAL
                },
                enabled = owned || affordable,
                height = Dimens.ButtonHeightCompact,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
