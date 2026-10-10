package com.mision.app.presentation.screen.shop

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.core.text.plural
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.ShopItem
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.AnimatedPet
import com.mision.app.presentation.components.CoinPill
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.EmptyState
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassChip
import com.mision.app.presentation.components.GlassDialogSurface
import com.mision.app.presentation.components.GradientProgressBar
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.ShopItemArt
import com.mision.app.presentation.components.TintedGlassSurface
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.CoinGold
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.ShopUiState
import com.mision.app.presentation.viewmodel.ShopViewModel

/** Narrowest a product card may get before the grid drops a column. */
private val ProductCardMinWidth = 132.dp
private const val MAX_GRID_COLUMNS = 4

/** Icon shown next to each category, so filters are not told apart by text alone. */
internal val CosmeticSlot.emoji: String
    get() = when (this) {
        CosmeticSlot.HAT -> "🎩"
        CosmeticSlot.ACCESSORY -> "🕶️"
        CosmeticSlot.BACKGROUND -> "🌅"
        CosmeticSlot.COLOR -> "🎨"
        CosmeticSlot.EFFECT -> "✨"
        CosmeticSlot.EMOTE -> "👋"
        CosmeticSlot.SKIN -> "🌌"
    }

/**
 * Tienda: cosmetics for Nube bought with coins earned in missions. There is
 * no real money anywhere in the app.
 *
 * Buying always goes through the preview sheet, which doubles as the
 * purchase confirmation; equipping and removing owned items is immediate.
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
            petRepository = container.petRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busyItemId by viewModel.busyItemId.collectAsStateWithLifecycle()
    val petName = state.pet?.name ?: "tu mascota"

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

    state.previewItem?.let { item ->
        ItemPreviewDialog(
            item = item,
            state = state,
            actionsEnabled = busyItemId == null,
            onAction = { viewModel.onItemAction(item) },
            onDismiss = viewModel::closePreview,
        )
    }

    MisionScreen(
        title = "Tienda",
        subtitle = "El armario de $petName",
        onBack = onBack,
        actions = { CoinPill(coins = state.coins) },
    ) {
        ShopHero(state)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            GlassChip(
                label = "Todo",
                leadingEmoji = "☁️",
                selected = state.selectedSlot == null,
                onClick = { viewModel.onSlotSelected(null) },
            )
            ShopCatalog.categoryOrder.forEach { slot ->
                GlassChip(
                    label = slot.displayName,
                    leadingEmoji = slot.emoji,
                    selected = state.selectedSlot == slot,
                    onClick = {
                        viewModel.onSlotSelected(if (state.selectedSlot == slot) null else slot)
                    },
                )
            }
        }

        val items = state.visibleItems
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Space3xl),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(Dimens.IconXl))
            }
            items.isEmpty() -> EmptyState(
                emoji = "☁️",
                title = "Nada por aquí todavía",
                message = "No hay artículos en esta categoría. Prueba con otra o vuelve más tarde.",
                actionLabel = "Ver todo",
                onAction = { viewModel.onSlotSelected(null) },
            )
            else -> ProductGrid(
                items = items,
                state = state,
                busyItemId = busyItemId,
                onOpen = viewModel::openPreview,
                onEquipToggle = viewModel::onItemAction,
            )
        }
    }
}

/** Nube wearing its current look, plus how much of the collection is unlocked. */
@Composable
private fun ShopHero(state: ShopUiState) {
    val pet = state.pet
    TintedGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            if (pet != null) {
                AnimatedPet(
                    pet = pet,
                    size = 104.dp,
                    accessibilityLabel = "${pet.name} con su aspecto actual",
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                Text(
                    text = "¿Qué me pruebo hoy?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Toca un artículo para verlo puesto antes de comprarlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.items.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpaceXs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Colección",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${state.ownedCount} de ${state.items.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                    GradientProgressBar(
                        progress = state.ownedCount.toFloat() / state.items.size,
                        colors = AppGradients.primary,
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
    onOpen: (ShopItem) -> Unit,
    onEquipToggle: (ShopItem) -> Unit,
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
                        val owned = state.isOwned(item)
                        ProductCard(
                            item = item,
                            owned = owned,
                            equipped = state.isEquipped(item),
                            affordable = state.canAfford(item),
                            missingCoins = (item.cost - state.coins).coerceAtLeast(0),
                            actionsEnabled = busyItemId == null,
                            onOpen = { onOpen(item) },
                            // Owned items toggle right away; buying needs the preview.
                            onAction = { if (owned) onEquipToggle(item) else onOpen(item) },
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
    onOpen: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.RadiusMd)
    val ring = MaterialTheme.colorScheme.primary
    // Drawn over the card so the glass fill cannot hide it.
    val highlight = if (equipped) {
        Modifier.drawWithContent {
            drawContent()
            drawOutline(shape.createOutline(size, layoutDirection, this), ring, style = Stroke(2.dp.toPx()))
        }
    } else {
        Modifier
    }
    GlassCard(
        modifier = modifier.then(highlight),
        shape = shape,
        contentPadding = PaddingValues(Dimens.SpaceMd),
        onClick = onOpen,
        onClickLabel = "Ver vista previa",
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            ShopItemArt(item = item) {
                when {
                    equipped -> StatusBadge(
                        text = "Equipado",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Dimens.SpaceSm),
                    )
                    !owned && !affordable -> Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Bloqueado",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Dimens.SpaceSm)
                            .size(Dimens.IconMd),
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
                    text = "${item.category.emoji} ${item.category.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (owned) OwnedTag() else PriceTag(cost = item.cost, affordable = affordable)
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

/**
 * Preview + purchase confirmation: Nube wearing the item over its current
 * look, the price, the balance that would remain and the single action that
 * applies (buy, equip or remove).
 */
@Composable
private fun ItemPreviewDialog(
    item: ShopItem,
    state: ShopUiState,
    actionsEnabled: Boolean,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    val owned = state.isOwned(item)
    val equipped = state.isEquipped(item)
    val affordable = state.canAfford(item)
    val previewPet: Pet = state.tryOn(item)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GlassDialogSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space2xl),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusSm))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                ),
                            ),
                        )
                        .padding(Dimens.SpaceSm),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedPet(
                        pet = previewPet,
                        size = 200.dp,
                        accessibilityLabel = "Vista previa: ${previewPet.name} con ${item.name}",
                    )
                }

                Text(
                    text = "${item.category.emoji} ${item.category.displayName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = when {
                        equipped -> "Lo llevas puesto ahora mismo."
                        owned -> "Ya es tuyo. Puedes ponértelo cuando quieras."
                        affordable -> "Cuesta ${plural(item.cost, "moneda", "monedas")}. " +
                            "Te quedarán ${plural(state.coins - item.cost, "moneda", "monedas")}."
                        else -> "Cuesta ${plural(item.cost, "moneda", "monedas")}. " +
                            "Te faltan ${plural(item.cost - state.coins, "moneda", "monedas")}: " +
                            "completa misiones para conseguirlas."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Column(
                    modifier = Modifier.widthIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    GlassButton(
                        text = when {
                            equipped -> "Quitar"
                            owned -> "Equipar"
                            affordable -> "Comprar por ${plural(item.cost, "moneda", "monedas")}"
                            else -> "Monedas insuficientes"
                        },
                        onClick = onAction,
                        style = if (equipped) GlassButtonStyle.TONAL else GlassButtonStyle.PRIMARY,
                        enabled = actionsEnabled && (owned || affordable),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GlassButton(
                        text = "Cerrar",
                        onClick = onDismiss,
                        style = GlassButtonStyle.TONAL,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Cost in coins, styled like the balance pill so the two read as the same currency. */
@Composable
private fun PriceTag(cost: Int, affordable: Boolean) {
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
            color = if (affordable) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = androidx.compose.ui.graphics.Color.White,
        modifier = modifier
            .clip(shape)
            .background(Brush.horizontalGradient(AppGradients.primary), shape)
            .padding(horizontal = Dimens.SpaceSm, vertical = 2.dp),
    )
}
