package com.mision.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mision.app.core.time.SpanishLocale
import com.mision.app.presentation.theme.Dimens

/** Hour (0-23) and minute (steps of 5) picker built from simple controls. */
@Composable
fun TimeStepper(
    hour: Int,
    minute: Int,
    onChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
        StepperRow(
            label = "Hora",
            value = String.format(SpanishLocale, "%02d", hour),
            onDecrease = { onChange((hour - 1 + 24) % 24, minute) },
            onIncrease = { onChange((hour + 1) % 24, minute) },
        )
        StepperRow(
            label = "Minuto",
            value = String.format(SpanishLocale, "%02d", minute),
            onDecrease = { onChange(hour, (minute - 5 + 60) % 60) },
            onIncrease = { onChange(hour, (minute + 5) % 60) },
        )
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassButton(
                text = "−",
                onClick = onDecrease,
                style = GlassButtonStyle.TONAL,
                height = Dimens.ButtonHeightCompact,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            GlassButton(
                text = "+",
                onClick = onIncrease,
                style = GlassButtonStyle.TONAL,
                height = Dimens.ButtonHeightCompact,
            )
        }
    }
}
