package com.mision.app.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.mision.app.presentation.theme.Dimens

enum class MisionButtonStyle { PRIMARY, TONAL, OUTLINE, DESTRUCTIVE }

/**
 * The app button. Built on Material 3 buttons, so disabled, pressed and
 * focused states, ripple and semantics are all standard.
 */
@Composable
fun MisionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: MisionButtonStyle = MisionButtonStyle.PRIMARY,
    leadingIcon: ImageVector? = null,
    height: Dp = Dimens.ButtonHeight,
) {
    val sized = modifier.heightIn(min = height)
    val padding = PaddingValues(horizontal = Dimens.SpaceXl)
    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(Dimens.IconMd))
            Spacer(Modifier.size(Dimens.SpaceSm))
        }
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    when (style) {
        MisionButtonStyle.PRIMARY ->
            Button(onClick, sized, enabled, contentPadding = padding, content = content)
        MisionButtonStyle.TONAL ->
            FilledTonalButton(onClick, sized, enabled, colors = brandTonalButtonColors(), contentPadding = padding, content = content)
        MisionButtonStyle.OUTLINE ->
            OutlinedButton(onClick, sized, enabled, contentPadding = padding, content = content)
        MisionButtonStyle.DESTRUCTIVE -> Button(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            contentPadding = padding,
            content = content,
        )
    }
}

/** Icon-only action (top bars, card actions). Always 48dp for touch. */
@Composable
fun MisionIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconButton(
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        onClick = onClick,
        modifier = modifier.size(Dimens.TouchTargetMin),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(Dimens.IconLg))
    }
}

/**
 * Selectable chip for filters and multi-choice settings. Selection is shown
 * with colour *and* a check mark, never by colour alone.
 */
@Composable
fun MisionFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        modifier = modifier,
        leadingIcon = when {
            selected -> {
                { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
            }
            icon != null -> {
                { Icon(icon, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
            }
            else -> null
        },
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

/** Tonal buttons use the brand container, keeping teal for data accents only. */
@Composable
fun brandTonalButtonColors() = ButtonDefaults.filledTonalButtonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)
