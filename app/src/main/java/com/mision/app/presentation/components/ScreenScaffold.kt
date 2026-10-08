package com.mision.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mision.app.presentation.LocalNavBottomPadding
import com.mision.app.presentation.theme.Dimens

/**
 * Standard screen layout: backdrop, floating glass top bar and a scrollable
 * content column. Every screen builds on it so spacing, padding and the back
 * affordance never drift apart.
 */
@Composable
fun MisionScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        MisionBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // On tablets and landscape the content keeps a readable width
            // instead of stretching edge to edge.
            MisionTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                actions = actions,
                modifier = Modifier.widthIn(max = Dimens.ContentMaxWidth),
            )
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (scrollable) Modifier.verticalScroll(scrollState) else Modifier,
                    )
                    .navigationBarsPadding()
                    .padding(
                        start = Dimens.ScreenHorizontalPadding,
                        end = Dimens.ScreenHorizontalPadding,
                        top = Dimens.SpaceMd,
                        // Clears the floating bottom bar on the tab routes.
                        bottom = Dimens.Space4xl + LocalNavBottomPadding.current,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = Dimens.ContentMaxWidth)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
                    content = content,
                )
            }
        }
    }
}

/** Floating glass bar with an optional back button and trailing actions. */
@Composable
fun MisionTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ScreenHorizontalPadding,
                end = Dimens.ScreenHorizontalPadding,
                top = Dimens.SpaceLg,
                bottom = Dimens.SpaceSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        if (onBack != null) {
            GlassIconButton(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                onClick = onBack,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (onBack == null) Dimens.SpaceXs else 0.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
    }
}

