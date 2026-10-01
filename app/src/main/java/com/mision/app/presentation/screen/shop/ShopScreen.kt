package com.mision.app.presentation.screen.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PetMood
import com.mision.app.domain.model.ShopItem
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.EmptyState
import com.mision.app.presentation.components.IconBadge
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionCard
import com.mision.app.presentation.components.MisionFilterChip
import com.mision.app.presentation.components.MisionIcons
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.SnackbarMessageEffect
import com.mision.app.presentation.components.icon
import com.mision.app.presentation.components.parseHexColor
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors
import com.mision.app.presentation.viewmodel.ShopItemUi
import com.mision.app.presentation.viewmodel.ShopUiState
import com.mision.app.presentation.viewmodel.ShopViewModel

/**
 * Tienda: cosmetics for the pet, bought with the coins earned in missions.
 * There is no real money anywhere in the app, so no payment methods exist.
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
    val busyItemId by viewModel.busyItemId.collectAsStateWithLifecycle()
    val pendingPurchase by viewModel.pendingPurchase.collectAsStateWithLifecycle()
    val message by viewModel.messages.current.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    SnackbarMessageEffect(message, snackbar, viewModel.messages::consumed)

    pendingPurchase?.let { entry ->
        ConfirmDialog(
            title = "¿Comprar ${entry.item.name}?",
            message = "Cuesta ${entry.item.cost} monedas. Te quedarán ${state.coins - entry.item.cost}.",
            confirmLabel = "Comprar",
            onConfirm = viewModel::confirmPurchase,
            onDismiss = viewModel::dismissPurchase,
            content = {
                ItemPreview(
                    item = entry.item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium),
                )
            },
        )
    }

    MisionScreen(
        title = "Tienda",
        subtitle = "Recompensas para tu mascota",
        onBack = onBack,
        snackbarHostState = snackbar,
    ) {
        BalanceCard(state = state)
        CategoryFilter(state = state, onSelect = viewModel::onCategorySelected)

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.Space3xl),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.items.isEmpty() -> MisionCard(modifier = Modifier.fillMaxWidth()) {
                EmptyState(
                    icon = Icons.Outlined.Storefront,
                    title = "Nada por aquí todavía",
                    message = "No hay artículos en esta categoría.",
                    actionLabel = "Ver todo",
                    onAction = { viewModel.onCategorySelected(null) },
                )
            }

            else -> ProductGrid(
                items = state.items,
                busyItemId = busyItemId,
                onAction = viewModel::onItemAction,
            )
        }
    }
}

@Composable
private fun BalanceCard(state: ShopUiState) {
    val game = MisionColors.game
    MisionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            IconBadge(
                icon = MisionIcons.Coin,
                size = Dimens.IconContainerLg,
                containerColor = game.coin,
                contentColor = game.onCoin,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tu saldo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.coins} monedas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "${state.ownedCount}/${state.totalCount}\nconseguidos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "Ganas monedas completando misiones, rachas y logros. Aquí no se usa dinero real.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.SpaceMd),
        )
    }
}

@Composable
private fun CategoryFilter(state: ShopUiState, onSelect: (CosmeticSlot?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        MisionFilterChip(
            label = "Todo",
            selected = state.selectedCategory == null,
            onClick = { onSelect(null) },
        )
        state.categories.forEach { category ->
            MisionFilterChip(
                label = category.displayName,
                icon = category.icon,
                selected = state.selectedCategory == category,
                onClick = { onSelect(if (state.selectedCategory == category) null else category) },
            )
        }
    }
}

/** Two columns on phones, three when the content area is wide enough. */
@Composable
private fun ProductGrid(
    items: List<ShopItemUi>,
    busyItemId: String?,
    onAction: (ShopItemUi) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= WIDE_GRID) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            items.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                    row.forEach { entry ->
                        ProductCard(
                            entry = entry,
                            busy = busyItemId == entry.item.id,
                            onAction = { onAction(entry) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    entry: ShopItemUi,
    busy: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = entry.item
    MisionCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box {
            ItemPreview(item = item, modifier = Modifier.fillMaxWidth())
            IconBadge(
                icon = item.category.icon,
                size = Dimens.IconContainer - Dimens.SpaceSm,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Dimens.SpaceSm),
            )
            if (entry.owned) {
                StatusBadge(
                    equipped = entry.equipped,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Dimens.SpaceSm),
                )
            }
        }
        Column(
            modifier = Modifier.padding(Dimens.SpaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PriceRow(entry = entry)
            MisionButton(
                text = when {
                    busy -> "Un momento…"
                    entry.equipped -> "Quitar"
                    entry.owned -> "Equipar"
                    entry.affordable -> "Comprar"
                    else -> "Te faltan ${entry.missingCoins}"
                },
                onClick = onAction,
                enabled = !busy,
                style = when {
                    entry.equipped -> MisionButtonStyle.OUTLINE
                    entry.owned -> MisionButtonStyle.TONAL
                    entry.affordable -> MisionButtonStyle.PRIMARY
                    else -> MisionButtonStyle.OUTLINE
                },
                height = Dimens.ButtonHeightCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.SpaceSm),
            )
        }
    }
}

/** Product image: the pet wearing exactly that item, on its colour swatch. */
@Composable
private fun ItemPreview(item: ShopItem, modifier: Modifier = Modifier) {
    val swatch = parseHexColor(item.previewColorHex, MaterialTheme.colorScheme.primary)
    val preview = remember(item.id) {
        Pet.default(epochDay = 0).copy(
            mood = PetMood.FELIZ,
            equipped = EquippedCosmetics().with(item.category, item.id),
        )
    }
    Box(
        modifier = modifier
            .height(Dimens.ShopPreviewHeight)
            .background(swatch.copy(alpha = SWATCH_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedPet(
            pet = preview,
            size = Dimens.PetPreview,
            animate = false,
            accessibilityLabel = "Vista previa: ${item.name}",
        )
    }
}

@Composable
private fun PriceRow(entry: ShopItemUi) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = if (entry.owned) "En tu colección" else "Precio: ${entry.item.cost} monedas"
        },
    ) {
        if (entry.owned) {
            Text(
                text = "En tu colección",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(
                imageVector = MisionIcons.Coin,
                contentDescription = null,
                tint = MisionColors.game.coin,
                modifier = Modifier.size(Dimens.IconSm),
            )
            Text(
                text = entry.item.cost.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun StatusBadge(equipped: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val container: Color = if (equipped) colors.primary else colors.surfaceContainerLowest
    val content: Color = if (equipped) colors.onPrimary else colors.onSurface
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        if (equipped) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = content, modifier = Modifier.size(Dimens.IconSm))
        }
        Text(
            text = if (equipped) "Equipado" else "Tuyo",
            style = MaterialTheme.typography.labelSmall,
            color = content,
        )
    }
}

private val WIDE_GRID = 560.dp
private const val SWATCH_ALPHA = 0.22f
