package com.mision.app.presentation.screen.missions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import com.mision.app.domain.model.MissionStatus
import com.mision.app.domain.usecase.MISSION_TITLE_MAX_LENGTH
import com.mision.app.presentation.LocalAppContainer
import com.mision.app.presentation.components.CelebrationDialog
import com.mision.app.presentation.components.ConfirmDialog
import com.mision.app.presentation.components.EmptyState
import com.mision.app.presentation.components.MisionButton
import com.mision.app.presentation.components.MisionButtonStyle
import com.mision.app.presentation.components.MisionCard
import com.mision.app.presentation.components.MisionFilterChip
import com.mision.app.presentation.components.MisionIconButton
import com.mision.app.presentation.components.MisionScreen
import com.mision.app.presentation.components.MissionCard
import com.mision.app.presentation.components.OnResumeEffect
import com.mision.app.presentation.components.ProgressBar
import com.mision.app.presentation.components.SectionHeader
import com.mision.app.presentation.components.SnackbarMessageEffect
import com.mision.app.presentation.components.icon
import com.mision.app.presentation.theme.Dimens
import com.mision.app.presentation.theme.MisionColors
import com.mision.app.presentation.viewmodel.MissionEditorState
import com.mision.app.presentation.viewmodel.MissionsUiState
import com.mision.app.presentation.viewmodel.MissionsViewModel

/**
 * Misiones: today's list with search, filters and full CRUD for custom
 * missions through the editor sheet.
 */
@Composable
fun MissionsScreen() {
    val container = LocalAppContainer.current
    val viewModel: MissionsViewModel = viewModel(
        factory = MissionsViewModel.factory(
            useCases = container.useCases,
            settingsRepository = container.settingsRepository,
            clock = container.clock,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val message by viewModel.messages.current.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    OnResumeEffect(viewModel::refresh)
    SnackbarMessageEffect(message, snackbar, viewModel.messages::consumed)

    celebration?.let { CelebrationDialog(celebration = it, onDismiss = viewModel::dismissCelebration) }

    MisionScreen(
        title = "Misiones",
        subtitle = "${state.completedCount} de ${state.totalToday} completadas hoy",
        snackbarHostState = snackbar,
        actions = {
            MisionIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Crear una misión",
                onClick = { viewModel.openEditor() },
            )
        },
    ) {
        DayProgress(state)

        OutlinedTextField(
            value = state.filter.query,
            onValueChange = viewModel::onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar misiones") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
        )

        Filters(
            state = state,
            onStatus = viewModel::onStatusFilter,
            onCategory = viewModel::onCategoryFilter,
            onClear = viewModel::clearFilters,
        )

        val visible = state.visibleMissions
        when {
            state.isLoading -> Unit
            visible.isEmpty() -> MisionCard(modifier = Modifier.fillMaxWidth()) {
                if (state.filter.isActive) {
                    EmptyState(
                        icon = Icons.Outlined.FilterAltOff,
                        title = "No hay misiones con esos filtros",
                        message = "Prueba a quitar algún filtro para verlas todas.",
                        actionLabel = "Quitar filtros",
                        onAction = viewModel::clearFilters,
                    )
                } else {
                    EmptyState(
                        icon = Icons.Outlined.Spa,
                        title = "No hay misiones hoy",
                        message = "Crea una misión personalizada y empieza a sumar XP.",
                        actionLabel = "Crear misión",
                        actionIcon = Icons.Filled.Add,
                        onAction = { viewModel.openEditor() },
                    )
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                visible.forEach { mission ->
                    MissionCard(
                        mission = mission,
                        onToggle = { viewModel.onToggleMission(mission) },
                        // Only custom missions are editable; built-in ones are not tappable.
                        onClick = if (mission.isCustom) ({ viewModel.openEditor(mission) }) else null,
                        onClickLabel = "Editar misión",
                    )
                }
            }
        }
    }

    state.editor?.let { editor ->
        MissionEditor(editor = editor, viewModel = viewModel)
    }
}

@Composable
private fun DayProgress(state: MissionsUiState) {
    val allDone = state.totalToday > 0 && state.completedCount == state.totalToday
    MisionCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "Progreso de hoy")
        ProgressBar(
            progress = if (state.totalToday == 0) 0f else state.completedCount.toFloat() / state.totalToday,
            label = "Progreso de hoy: ${state.completedCount} de ${state.totalToday}",
            color = if (allDone) MisionColors.game.success else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = Dimens.SpaceSm),
        )
        val remaining = state.totalToday - state.completedCount
        Text(
            text = when {
                state.totalToday == 0 -> "Todavía no hay misiones para hoy."
                allDone -> "¡Perfecto! Has completado todas las misiones."
                remaining == 1 -> "Te queda 1 misión."
                else -> "Te quedan $remaining misiones."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Filters(
    state: MissionsUiState,
    onStatus: (MissionStatus?) -> Unit,
    onCategory: (MissionCategory?) -> Unit,
    onClear: () -> Unit,
) {
    val filter = state.filter
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            MisionFilterChip(
                label = "Todas",
                selected = filter.category == null && filter.status == null,
                onClick = onClear,
            )
            MissionStatus.entries.forEach { status ->
                MisionFilterChip(
                    label = status.pluralLabel,
                    selected = filter.status == status,
                    onClick = { onStatus(if (filter.status == status) null else status) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        ) {
            state.categories.forEach { category ->
                MisionFilterChip(
                    label = category.displayName,
                    icon = category.icon,
                    selected = filter.category == category,
                    onClick = { onCategory(if (filter.category == category) null else category) },
                )
            }
        }
    }
}

private val MissionStatus.pluralLabel: String
    get() = when (this) {
        MissionStatus.PENDIENTE -> "Pendientes"
        MissionStatus.COMPLETADA -> "Completadas"
    }

/** Create / edit sheet for custom missions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MissionEditor(editor: MissionEditorState, viewModel: MissionsViewModel) {
    if (editor.confirmDelete) {
        ConfirmDialog(
            title = "¿Eliminar esta misión?",
            message = "Desaparecerá de hoy y de los próximos días. Las recompensas ya ganadas se conservan.",
            confirmLabel = "Eliminar",
            destructive = true,
            onConfirm = viewModel::deleteEditorMission,
            onDismiss = { viewModel.onEditorDeleteRequest(false) },
        )
    }

    Dialog(
        onDismissRequest = viewModel::closeEditor,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .widthIn(max = Dimens.ContentMaxWidth)
                .fillMaxWidth()
                .heightIn(max = Dimens.DialogMaxHeight)
                .padding(Dimens.SpaceLg)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.Space2xl),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (editor.isEditing) "Editar misión" else "Nueva misión",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                    )
                    TextButton(onClick = viewModel::closeEditor) { Text("Cerrar") }
                }

                OutlinedTextField(
                    value = editor.title,
                    onValueChange = viewModel::onEditorTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de la misión") },
                    singleLine = true,
                    isError = editor.titleError != null,
                    supportingText = {
                        Text(editor.titleError ?: "${editor.title.length}/$MISSION_TITLE_MAX_LENGTH")
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = editor.description,
                    onValueChange = viewModel::onEditorDescription,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )

                EditorSection(title = "Categoría") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                    ) {
                        MissionCategory.entries.forEach { category ->
                            MisionFilterChip(
                                label = category.displayName,
                                icon = category.icon,
                                selected = editor.category == category,
                                onClick = { viewModel.onEditorCategory(category) },
                            )
                        }
                    }
                }

                EditorSection(title = "Dificultad (define la recompensa)") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
                    ) {
                        MissionDifficulty.entries.forEach { difficulty ->
                            MisionFilterChip(
                                label = "${difficulty.displayName} · +${difficulty.xpReward} XP",
                                selected = editor.difficulty == difficulty,
                                onClick = { viewModel.onEditorDifficulty(difficulty) },
                            )
                        }
                    }
                }

                EditorSection(
                    title = if (editor.durationMinutes == 0) "Duración: sin indicar" else "Duración: ${editor.durationMinutes} min",
                ) {
                    Slider(
                        value = editor.durationMinutes.toFloat(),
                        onValueChange = { viewModel.onEditorDuration(it.toInt()) },
                        valueRange = 0f..MAX_DURATION_MINUTES,
                        steps = DURATION_STEPS,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repetir cada día",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (editor.isRecurring) "Aparecerá en tu lista todos los días." else "Solo para hoy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = editor.isRecurring, onCheckedChange = viewModel::onEditorRecurring)
                }

                MisionButton(
                    text = if (editor.isEditing) "Guardar cambios" else "Crear misión",
                    onClick = viewModel::saveEditor,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (editor.isEditing) {
                    MisionButton(
                        text = "Eliminar misión",
                        onClick = { viewModel.onEditorDeleteRequest(true) },
                        style = MisionButtonStyle.OUTLINE,
                        leadingIcon = Icons.Filled.Delete,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

private const val MAX_DURATION_MINUTES = 120f
private const val DURATION_STEPS = 11
