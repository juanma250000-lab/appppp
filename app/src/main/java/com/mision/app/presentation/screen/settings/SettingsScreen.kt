package com.mision.app.presentation.screen.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.BuildConfig
import com.mision.app.core.time.DateFormats
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.ThemeMode
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.BrandMark
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionCard
import com.mision.app.presentation.components.MisionFilterChip
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.SnackbarMessageEffect
import com.mision.app.presentation.components.icon
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.SettingsViewModel

/** Ajustes: appearance, reminders, favourite categories and the data reset. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(
            settingsRepository = container.settingsRepository,
            useCases = container.useCases,
        ),
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.messages.current.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    SnackbarMessageEffect(message, snackbar, viewModel.messages::consumed)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.setNotificationsEnabled(true) else viewModel.onNotificationPermissionDenied()
    }
    val enableReminder: () -> Unit = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setNotificationsEnabled(true)
        }
    }

    MisionScreen(
        title = "Ajustes",
        subtitle = "Personaliza Misión a tu gusto",
        onBack = onBack,
        snackbarHostState = snackbar,
    ) {
        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Apariencia")
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceMd),
            ) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) { Text(mode.displayName) }
                }
            }
            // Material You palettes only exist from Android 12.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchRow(
                    title = "Colores dinámicos",
                    subtitle = "Usa la paleta de tu fondo de pantalla.",
                    checked = settings.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }
            SwitchRow(
                title = "Animaciones",
                subtitle = "Movimiento de la mascota y confeti de celebración.",
                checked = settings.animationsEnabled,
                onCheckedChange = viewModel::setAnimationsEnabled,
            )
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Recordatorio")
            SwitchRow(
                title = "Recordatorio diario",
                subtitle = "Un aviso cada día para no perder tu racha.",
                checked = settings.notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) enableReminder() else viewModel.setNotificationsEnabled(false)
                },
            )
            if (settings.notificationsEnabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.TouchTargetMin)
                        .clickable(onClickLabel = "Cambiar la hora", role = Role.Button) { showTimePicker = true }
                        .padding(vertical = Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Hora del aviso",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = DateFormats.hourMinute(settings.reminderHour, settings.reminderMinute),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Categorías favoritas")
            Text(
                text = "Las misiones de estas categorías aparecen primero en tu lista del día.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.padding(top = Dimens.SpaceSm),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                MissionCategory.entries.forEach { category ->
                    MisionFilterChip(
                        label = category.displayName,
                        icon = category.icon,
                        selected = category in settings.preferredCategories,
                        onClick = { viewModel.toggleFavourite(category) },
                    )
                }
            }
        }

        MisionCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(title = "Progreso")
            Text(
                text = "Restablecer borra misiones, XP, monedas, racha, logros y cosméticos comprados. Tus ajustes se conservan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MisionButton(
                text = "Restablecer todo el progreso",
                onClick = { showResetDialog = true },
                style = MisionButtonStyle.DESTRUCTIVE,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.SpaceMd),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd, Alignment.CenterHorizontally),
        ) {
            BrandMark()
            Column {
                Text("Misión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Versión ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = "¿Restablecer todo el progreso?",
            message = "Se eliminarán tus misiones, experiencia, monedas, racha, logros y compras. Esta acción no se puede deshacer.",
            confirmLabel = "Sí, restablecer",
            destructive = true,
            onConfirm = {
                showResetDialog = false
                viewModel.resetProgress()
            },
            onDismiss = { showResetDialog = false },
        )
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            onConfirm = { hour, minute ->
                showTimePicker = false
                viewModel.setReminderTime(hour, minute)
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

/** Labeled switch; the whole row is the touch target. */
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // The row handles the toggle, so the switch itself is not a second target.
        Switch(checked = checked, onCheckedChange = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    hour: Int,
    minute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hora del recordatorio") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
