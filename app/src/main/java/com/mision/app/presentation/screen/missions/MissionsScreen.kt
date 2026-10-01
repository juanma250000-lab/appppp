package com.mision.app.presentation.screen.missions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import com.mision.app.domain.model.MissionStatus
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.CelebrationDialog
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.GlassButton
import com.mision.app.presentation.components.GlassButtonStyle
import com.mision.app.presentation.components.GlassCard
import com.mision.app.presentation.components.GlassChip
import com.mision.app.presentation.components.GlassIconButton
import com.mision.app.presentation.components.GradientProgressBar
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.MissionCard
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.celebrations.CelebrationDispatcher
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.viewmodel.MissionEditorState
import com.mision.app.presentation.viewmodel.MissionsViewModel

/**
 * Misiones: today's list with search, filters and full CRUD for custom
 * missions through a glass editor sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissionsScreen() {
    val container = LocalAppContainer.current
    val viewModel: MissionsViewModel = viewModel(
        factory = MissionsViewModel.factory(
            useCases = container.useCases,
            clock = container.clock,
            celebrationDispatcher = CelebrationDispatcher(
                notifier = container.notifier,
                settingsRepository = container.settingsRepository,
            ),
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val completion by viewModel.completion.collectAsStateWithLifecycle()
    val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { container.useCases.ensureDailyMissions() }

    completion?.let { dialog ->
        CelebrationDialog(
            emoji = dialog.emoji,
            title = dialog.title,
            message = dialog.message,
            onDismiss = viewModel::dismissCelebration,
            confirmLabel = "¡Genial!",
        )
    }

    infoMessage?.let { message ->
        ConfirmDialog(
            title = "Aviso",
            message = message,
            confirmLabel = "Entendido",
            onConfirm = viewModel::dismissInfo,
            onDismiss = viewModel::dismissInfo,
            dismissLabel = "Cerrar",
        )
    }

    val deletingCustomMission = state.editor?.let { editor ->
        editor.templateId != null && editor.isCustom
    } ?: false

    MisionScreen(
        title = "Misiones",
        subtitle = "${state.completedCount} de ${state.totalToday} completadas hoy",
        actions = {
            GlassIconButton(
                imageVector = Icons.Filled.Add,
                contentDescription = "Crear una misión",
                onClick = { viewModel.openEditor() },
            )
        },
    ) {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                SectionHeader(title = "Progreso de hoy")
                GradientProgressBar(
                    progress = if (state.totalToday == 0) 0f
                    else state.completedCount.toFloat() / state.totalToday,
                    colors = if (state.totalToday > 0 && state.completedCount >= state.totalToday) {
                        AppGradients.celebrate
                    } else {
                        AppGradients.primary
                    },
                    label = "Progreso de hoy",
                )
                Text(
                    text = if (state.totalToday == 0) {
                        "Todavía no hay misiones para hoy."
                    } else if (state.completedCount >= state.totalToday) {
                        "¡Perfecto! Has completado todas las misiones."
                    } else {
                        "Te quedan ${state.totalToday - state.completedCount} misiones."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ---- Search + filters ---------------------------------------------
        OutlinedTextField(
            value = state.filter.query,
            onValueChange = viewModel::onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar misiones") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
            colors = glassTextFieldColors(),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            GlassChip(
                label = "Todas",
                selected = state.filter.category == null && state.filter.status == null,
                onClick = viewModel::clearFilters,
            )
            MissionCategory.entries.forEach { category ->
                GlassChip(
                    label = category.displayName,
                    leadingEmoji = category.emoji,
                    selected = state.filter.category == category,
                    onClick = {
                        viewModel.onCategoryFilter(
                            if (state.filter.category == category) null else category,
                        )
                    },
                )
            }
            MissionStatus.entries.forEach { status ->
                GlassChip(
                    label = status.displayName,
                    selected = state.filter.status == status,
                    onClick = {
                        viewModel.onStatusFilter(if (state.filter.status == status) null else status)
                    },
                )
            }
        }

        // ---- List ----------------------------------------------------------
        val visible = state.visibleMissions
        if (visible.isEmpty()) {
            GlassCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    Text(text = "🔍", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = if (state.filter.isActive) {
                            "No hay misiones con esos filtros"
                        } else {
                            "No hay misiones hoy"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (state.filter.isActive) {
                            "Prueba a quitar algún filtro para verlas todas."
                        } else {
                            "Crea una misión personalizada y empieza a sumar XP."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.filter.isActive) {
                        GlassButton(
                            text = "Quitar filtros",
                            onClick = viewModel::clearFilters,
                            style = GlassButtonStyle.TONAL,
                            height = Dimens.ButtonHeightCompact,
                        )
                    } else {
                        GlassButton(
                            text = "Crear misión",
                            onClick = { viewModel.openEditor() },
                            leadingIcon = Icons.Filled.Add,
                            height = Dimens.ButtonHeightCompact,
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                visible.forEach { mission ->
                    MissionCard(
                        mission = mission,
                        onToggle = { viewModel.onToggleMission(mission) },
                        onClick = {
                            if (mission.isCustom) viewModel.openEditor(mission)
                        },
                    )
                }
            }
        }
    }

    state.editor?.let { editor ->
        MissionEditorSheet(
            editor = editor,
            showDelete = deletingCustomMission,
            onTitle = viewModel::onEditorTitle,
            onDescription = viewModel::onEditorDescription,
            onCategory = viewModel::onEditorCategory,
            onDifficulty = viewModel::onEditorDifficulty,
            onDuration = viewModel::onEditorDuration,
            onRecurring = viewModel::onEditorRecurring,
            onReminder = viewModel::onEditorReminder,
            onReminderTime = viewModel::onEditorReminderTime,
            onSave = viewModel::saveEditor,
            onClose = viewModel::closeEditor,
            onDelete = viewModel::deleteEditorMission,
        )
    }
}

/** Full-screen editor rendered as a scrollable glass sheet. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MissionEditorSheet(
    editor: MissionEditorState,
    showDelete: Boolean,
    onTitle: (String) -> Unit,
    onDescription: (String) -> Unit,
    onCategory: (MissionCategory) -> Unit,
    onDifficulty: (MissionDifficulty) -> Unit,
    onDuration: (Int) -> Unit,
    onRecurring: (Boolean) -> Unit,
    onReminder: (Boolean) -> Unit,
    onReminderTime: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .padding(Dimens.SpaceLg),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.SpaceMd),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                SectionHeader(
                    title = if (editor.isEditing) "Editar misión" else "Nueva misión",
                    actionLabel = "Cerrar",
                    onAction = onClose,
                )

                OutlinedTextField(
                    value = editor.title,
                    onValueChange = onTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de la misión") },
                    singleLine = true,
                    isError = editor.isError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    colors = glassTextFieldColors(),
                )
                OutlinedTextField(
                    value = editor.description,
                    onValueChange = onDescription,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción (opcional)") },
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default,
                    ),
                    colors = glassTextFieldColors(),
                )

                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    MissionCategory.entries.forEach { category ->
                        GlassChip(
                            label = category.displayName,
                            leadingEmoji = category.emoji,
                            selected = editor.category == category,
                            onClick = { onCategory(category) },
                        )
                    }
                }

                Text(
                    text = "Dificultad (define la recompensa)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                ) {
                    MissionDifficulty.entries.forEach { difficulty ->
                        GlassChip(
                            label = "${difficulty.displayName} · +${difficulty.xpReward} XP",
                            selected = editor.difficulty == difficulty,
                            onClick = { onDifficulty(difficulty) },
                        )
                    }
                }

                Text(
                    text = "Duración estimada: ${editor.durationMinutes} minutos",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Slider(
                    value = editor.durationMinutes.toFloat(),
                    onValueChange = { onDuration(it.toInt()) },
                    valueRange = 0f..120f,
                    steps = 11,
                )

                GlassChip(
                    label = if (editor.isRecurring) "Se repite cada día" else "Solo un día",
                    selected = editor.isRecurring,
                    onClick = { onRecurring(!editor.isRecurring) },
                )

                GlassChip(
                    label = if (editor.reminderEnabled) "Recordatorio activado" else "Sin recordatorio",
                    selected = editor.reminderEnabled,
                    onClick = { onReminder(!editor.reminderEnabled) },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassButton(
                        text = if (editor.isEditing) "Guardar cambios" else "Crear misión",
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                    )
                    if (showDelete) {
                        GlassIconButton(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar misión",
                            onClick = onDelete,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun glassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)
