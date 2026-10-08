package com.mision.app.presentation.screen.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.AppSettings
import com.mision.app.core.time.SpanishLocale
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.ThemeMode
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassChip
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.SettingsViewModel

/** Ajustes: appearance, sound, reminders, categories and the data reset. */
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
    var showResetDialog by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.markNotificationPermissionRequested()
        permissionDenied = !granted
        if (granted) viewModel.setNotificationsEnabled(true)
    }

    // Reminders are only switched on once Android actually allows them;
    // otherwise the toggle would show "on" while nothing can ever be posted.
    val enableNotifications = {
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionDenied = false
            viewModel.setNotificationsEnabled(true)
        }
    }

    MisionScreen(
        title = "Ajustes",
        subtitle = "Personaliza Misión a tu gusto",
        onBack = onBack,
    ) {
        // ---- Appearance ---------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Apariencia")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        GlassChip(
                            label = mode.displayName,
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                        )
                    }
                }
                SwitchRow(
                    title = "Colores dinámicos",
                    subtitle = "Usa la paleta de tu fondo de pantalla.",
                    checked = settings.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }
        }

        // ---- Feedback -----------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Sonido y animaciones")
                SwitchRow(
                    title = "Sonidos",
                    subtitle = "Efectos al completar misiones.",
                    checked = settings.soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled,
                )
                SwitchRow(
                    title = "Animaciones",
                    subtitle = "Movimientos de la mascota y transiciones.",
                    checked = settings.animationsEnabled,
                    onCheckedChange = viewModel::setAnimationsEnabled,
                )
            }
        }

        // ---- Reminders ----------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Recordatorios")
                SwitchRow(
                    title = "Recordatorio diario",
                    subtitle = "Un aviso cada día para no perder tu racha.",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) enableNotifications() else viewModel.setNotificationsEnabled(false)
                    },
                )
                if (permissionDenied && !settings.notificationsEnabled) {
                    Text(
                        text = "Sin el permiso de notificaciones no podemos avisarte. " +
                            "Puedes concederlo desde los ajustes del sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (settings.notificationsEnabled) {
                    TimePickerRow(
                        hour = settings.reminderHour,
                        minute = settings.reminderMinute,
                        onChange = viewModel::setReminderTime,
                    )
                    Text(
                        text = "Aviso programado a las ${settings.reminderLabel}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    GlassButton(
                        text = "Activar notificaciones",
                        onClick = enableNotifications,
                        style = GlassButtonStyle.TONAL,
                        height = Dimens.ButtonHeightCompact,
                    )
                }
            }
        }

        // ---- Categories ---------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Categorías favoritas")
                Text(
                    text = "Marca las que más te interesan para tenerlas siempre a mano.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Wraps instead of squeezing: six labelled chips never fit on
                // one phone-width row.
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    MissionCategory.entries.forEach { category ->
                        GlassChip(
                            label = category.displayName,
                            leadingEmoji = category.emoji,
                            selected = category in settings.preferredCategories,
                            onClick = { toggleCategory(settings, category, viewModel) },
                        )
                    }
                }
            }
        }

        // ---- Danger zone ---------------------------------------------------
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                SectionHeader(title = "Progreso")
                Text(
                    text = "Restablecer borra misiones, XP, monedas, racha, logros y cosméticos comprados. Tus ajustes se conservan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GlassButton(
                    text = "Restablecer todo el progreso",
                    onClick = { showResetDialog = true },
                    style = GlassButtonStyle.DESTRUCTIVE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text(
            text = "Misión 1.0 · Hecho con cariño en español.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
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
            dismissLabel = "Cancelar",
        )
    }
}

private fun toggleCategory(
    settings: AppSettings,
    category: MissionCategory,
    viewModel: SettingsViewModel,
) {
    val updated = if (category in settings.preferredCategories) {
        settings.preferredCategories - category
    } else {
        settings.preferredCategories + category
    }
    viewModel.setPreferredCategories(updated)
}

/** Labeled switch row inside a glass card. */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = Dimens.SpaceMd),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

/** Hour (0-23) and minute (steps of 5) picker built from simple controls. */
@Composable
private fun TimePickerRow(
    hour: Int,
    minute: Int,
    onChange: (Int, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
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
