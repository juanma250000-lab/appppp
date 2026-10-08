package com.mision.app.presentation.screen.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.domain.model.ShopItem
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.CoinPill
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.EmptyState
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassChip
import com.mision.app.presentation.components.GradientProgressBar
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.ShopItemArt
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.CoinGold
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.ShopUiState
import com.mision.app.presentation.viewmodel.ShopViewModel

/** Narrowest a product card may get before the grid drops a column. */
private val ProductCardMinWidth = 150.dp
private const val MAX_GRID_COLUMNS = 4

/**
 * Tienda: cosmetics bought with coins earned in missions. There is no real
 * money anywhere in the app.
 */
@OptIn(ExperimentalLayoutApi::class)
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
    val busyItemId by viewModel.busyItemId.collectAsStateWithLifecycle()

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
        BalanceCard(state)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            GlassChip(
                label = "Todo",
                selected = state.selectedSlot == null,
                onClick = { viewModel.onSlotSelected(null) },
            )
            ShopCatalog.categoryOrder.forEach { slot ->
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
        when {
            state.isLoading -> Unit
            items.isEmpty() -> EmptyState(
                emoji = "🛍️",
                title = "Nada por aquí todavía",
                message = "No hay artículos en esta categoría. Prueba con otra o vuelve más tarde.",
                actionLabel = "Ver todo",
                onAction = { viewModel.onSlotSelected(null) },
            )
            else -> ProductGrid(
                items = items,
                state = state,
                busyItemId = busyItemId,
                onAction = viewModel::onItemAction,
            )
        }
    }
}

/** Balance plus collection progress, so the user knows where they stand. */
@Composable
private fun BalanceCard(state: ShopUiState) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu saldo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Gana monedas completando misiones y manteniendo tu racha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CoinPill(coins = state.coins)
            }
            if (state.items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Tu colección",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${state.ownedCount} de ${state.items.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    GradientProgressBar(
                        progress = state.ownedCount.toFloat() / state.items.size,
                        colors = AppGradients.coin,
                        height = 8.dp,
                        label = "Colección de la tienda",
                    )
                }
            }
        }
    }
}

/**
 * Responsive grid: as many columns as fit at [ProductCardMinWidth] (two on
 * phones, up to four on tablets). Cards in the same row share one height so
 * images, prices and buttons line up.
 */
@Composable
private fun ProductGrid(
    items: List<ShopItem>,
    state: ShopUiState,
    busyItemId: String?,
    onAction: (ShopItem) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = Dimens.SpaceMd
        val columns = ((maxWidth + gap) / (ProductCardMinWidth + gap))
            .toInt()
            .coerceIn(1, MAX_GRID_COLUMNS)

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            items.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    rowItems.forEach { item ->
                        ProductCard(
                            item = item,
                            owned = state.isOwned(item),
                            equipped = state.isEquipped(item),
                            affordable = state.canAfford(item),
                            missingCoins = (item.cost - state.coins).coerceAtLeast(0),
                            actionsEnabled = busyItemId == null,
                            onAction = { onAction(item) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                    // Keeps the last row's cards the same width as the others.
                    repeat(columns - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** IMAGE → NAME → DESCRIPTION → CATEGORY / PRICE → ACTION. */
@Composable
private fun ProductCard(
    item: ShopItem,
    owned: Boolean,
    equipped: Boolean,
    affordable: Boolean,
    missingCoins: Int,
    actionsEnabled: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(Dimens.SpaceMd),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            ShopItemArt(item = item) {
                if (equipped) {
                    StatusBadge(
                        text = "Equipado",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(Dimens.SpaceSm),
                    )
                }
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            // Pushes price and action to the bottom so they align across a row.
            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (owned) OwnedTag() else PriceTag(cost = item.cost)
            }

            GlassButton(
                text = when {
                    equipped -> "Quitar"
                    owned -> "Equipar"
                    affordable -> "Comprar"
                    else -> "Faltan $missingCoins"
                },
                onClick = onAction,
                style = when {
                    equipped -> GlassButtonStyle.TONAL
                    owned || affordable -> GlassButtonStyle.PRIMARY
                    else -> GlassButtonStyle.TONAL
                },
                enabled = actionsEnabled && (owned || affordable),
                height = Dimens.ButtonHeightCompact,
                contentPadding = PaddingValues(horizontal = Dimens.SpaceSm),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Cost in coins, styled like the balance pill so the two read as the same currency. */
@Composable
private fun PriceTag(cost: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = CoinGold,
            modifier = Modifier.size(Dimens.IconSm),
        )
        Text(
            text = cost.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun OwnedTag() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.IconSm),
        )
        Text(
            text = "Tuyo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Small pill pinned over the product image. */
@Composable
private fun StatusBadge(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.RadiusPill)
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(shape)
            .background(Brush.horizontalGradient(AppGradients.primary), shape)
            .padding(horizontal = Dimens.SpaceSm, vertical = 2.dp),
    )
}
