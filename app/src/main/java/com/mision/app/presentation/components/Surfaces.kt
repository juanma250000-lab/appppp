package com.mision.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.mision.app.R
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.isDark

/**
 * The building block of every screen: a flat surface with a hairline
 * outline. No blur, no heavy shadow: hierarchy comes from spacing and type.
 */
@Composable
fun MisionCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        border = BorderStroke(Dimens.Hairline, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Hero panel (pet stage): the only surface allowed a gradient, a soft wash of
 * the brand container colour that fades into the card.
 */
@Composable
fun HeroSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Dimens.SpaceLg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.surfaceContainerLowest,
        border = BorderStroke(Dimens.Hairline, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.primaryContainer.copy(alpha = if (colors.isDark) HERO_ALPHA_DARK else 1f),
                            colors.surfaceContainerLowest,
                        ),
                    ),
                )
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

/** Tinted circular container for a leading icon (lists, tiles, dialogs). */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.IconContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size * ICON_TO_CONTAINER),
        )
    }
}

/** The app logo (same artwork as the launcher icon). */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = Dimens.BrandMark) {
    Image(
        painter = painterResource(R.drawable.ic_brand_mark),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

private const val ICON_TO_CONTAINER = 0.55f
private const val HERO_ALPHA_DARK = 0.5f
