package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.AppResult
import com.mision.app.domain.model.Mission
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import com.mision.app.domain.model.MissionFilter
import com.mision.app.domain.model.MissionStatus
import com.mision.app.domain.repository.MissionDraft
import com.mision.app.domain.usecase.UseCases
import com.mision.app.presentation.celebrations.CelebrationUi
import com.mision.app.presentation.celebrations.CelebrationDispatcher
import com.mision.app.presentation.celebrations.toCelebrationUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Editor sheet state: `null` means the sheet is closed. */
data class MissionEditorState(
    val templateId: String? = null,
    val title: String = "",
    val description: String = "",
    val category: MissionCategory = MissionCategory.PERSONAL,
    val difficulty: MissionDifficulty = MissionDifficulty.FACIL,
    val durationMinutes: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val isRecurring: Boolean = true,
    val isError: Boolean = false,
    val isCustom: Boolean = true,
) {
    val isEditing: Boolean get() = templateId != null
}

data class MissionsUiState(
    val isLoading: Boolean = true,
    val missions: List<Mission> = emptyList(),
    val filter: MissionFilter = MissionFilter(),
    val completedCount: Int = 0,
    val totalToday: Int = 0,
    val editor: MissionEditorState? = null,
    val completion: MissionCompletionResultUi? = null,
    val infoMessage: String? = null,
) {
    val visibleMissions: List<Mission>
        get() = missions.filter { filter.matches(it) }
}

/** Lightweight celebration info surfaced on the missions screen. */
data class MissionCompletionResultUi(val celebration: CelebrationUi)

/** Missions screen: list, filters, search and custom mission CRUD. */
class MissionsViewModel(
    private val useCases: UseCases,
    private val clock: ClockProvider,
    private val celebrationDispatcher: CelebrationDispatcher,
) : ViewModel() {

    private val epochDay: Int = clock.todayEpochDay()

    private val _filter = MutableStateFlow(MissionFilter())
    private val _editor = MutableStateFlow<MissionEditorState?>(null)
    private val _completion = MutableStateFlow<CelebrationUi?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    val completion: StateFlow<CelebrationUi?> = _completion.asStateFlow()
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    val uiState: StateFlow<MissionsUiState> = combine(
        useCases.getDailyMissions(epochDay),
        _filter,
        _editor,
        _completion,
    ) { missions, filter, editor, completion ->
        MissionsUiState(
            isLoading = false,
            missions = missions,
            filter = filter,
            completedCount = missions.count { it.isCompleted },
            totalToday = missions.size,
            editor = editor,
            completion = completion?.let { MissionCompletionResultUi(it) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MissionsUiState(),
    )

    init {
        viewModelScope.launch { runCatching { useCases.ensureDailyMissions() } }
    }

    // ---- Filters ---------------------------------------------------------
    fun onCategoryFilter(category: MissionCategory?) {
        _filter.value = _filter.value.copy(category = category)
    }

    fun onStatusFilter(status: MissionStatus?) {
        _filter.value = _filter.value.copy(status = status)
    }

    fun onSearch(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun clearFilters() {
        _filter.value = MissionFilter()
    }

    // ---- Completion ------------------------------------------------------
    fun onToggleMission(mission: Mission) {
        viewModelScope.launch {
            if (mission.isCompleted) {
                useCases.uncompleteMission(mission.id)
            } else {
                val result = useCases.completeMission(mission.id) ?: return@launch
                celebrationDispatcher.dispatch(result)
                _completion.value = result.toCelebrationUi()
            }
        }
    }

    fun dismissCelebration() {
        _completion.value = null
    }

    fun dismissInfo() {
        _infoMessage.value = null
    }

    // ---- Editor ----------------------------------------------------------
    fun openEditor(mission: Mission? = null) {
        _editor.value = if (mission == null) {
            MissionEditorState()
        } else {
            MissionEditorState(
                templateId = mission.templateId,
                title = mission.title,
                description = mission.description,
                category = mission.category,
                difficulty = mission.difficulty,
                durationMinutes = mission.durationMinutes ?: 0,
                reminderEnabled = mission.reminderEnabled,
                reminderHour = mission.reminderHour,
                reminderMinute = mission.reminderMinute,
                isRecurring = mission.isRecurring,
                isCustom = mission.isCustom,
            )
        }
    }

    fun closeEditor() {
        _editor.value = null
    }

    fun onEditorTitle(value: String) = updateEditor { it.copy(title = value) }
    fun onEditorDescription(value: String) = updateEditor { it.copy(description = value) }
    fun onEditorCategory(value: MissionCategory) = updateEditor { it.copy(category = value) }
    fun onEditorDifficulty(value: MissionDifficulty) = updateEditor { it.copy(difficulty = value) }
    fun onEditorDuration(value: Int) = updateEditor { it.copy(durationMinutes = value) }
    fun onEditorRecurring(value: Boolean) = updateEditor { it.copy(isRecurring = value) }
    fun onEditorReminder(value: Boolean) = updateEditor { it.copy(reminderEnabled = value) }
    fun onEditorReminderTime(hour: Int, minute: Int) =
        updateEditor { it.copy(reminderHour = hour, reminderMinute = minute) }

    private fun updateEditor(transform: (MissionEditorState) -> MissionEditorState) {
        val current = _editor.value ?: return
        _editor.value = transform(current).copy(isError = false)
    }

    fun saveEditor() {
        val editor = _editor.value ?: return
        viewModelScope.launch {
            val draft = MissionDraft(
                title = editor.title.trim(),
                description = editor.description.trim(),
                category = editor.category,
                difficulty = editor.difficulty,
                durationMinutes = editor.durationMinutes.takeIf { it > 0 },
                reminderEnabled = editor.reminderEnabled,
                reminderHour = editor.reminderHour,
                reminderMinute = editor.reminderMinute,
                isRecurring = editor.isRecurring,
            )
            val result = if (editor.templateId == null) {
                useCases.createCustomMission(draft)
            } else {
                useCases.updateCustomMission(editor.templateId, draft)
            }
            when (result) {
                is AppResult.Success -> {
                    _editor.value = null
                    _infoMessage.value = if (editor.templateId == null) {
                        "Misión creada. Ya aparece en tu lista de hoy."
                    } else {
                        "Misión actualizada."
                    }
                }
                is AppResult.Error -> {
                    _editor.value = editor.copy(isError = true)
                    _infoMessage.value = result.message
                }
            }
        }
    }

    fun deleteEditorMission() {
        val id = _editor.value?.templateId ?: return
        viewModelScope.launch {
            val deleted = useCases.deleteCustomMission(id)
            _editor.value = null
            _infoMessage.value = if (deleted) {
                "Misión eliminada."
            } else {
                "Solo puedes eliminar las misiones que tú creaste."
            }
        }
    }

    companion object {
        fun factory(
            useCases: UseCases,
            clock: ClockProvider,
            celebrationDispatcher: CelebrationDispatcher,
        ) = viewModelFactory {
            initializer {
                MissionsViewModel(
                    useCases = useCases,
                    clock = clock,
                    celebrationDispatcher = celebrationDispatcher,
                )
            }
        }
    }
}
