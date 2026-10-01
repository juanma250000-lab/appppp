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
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.usecase.MISSION_TITLE_MAX_LENGTH
import com.mision.app.domain.usecase.UseCases
import com.mision.app.presentation.celebrations.CelebrationUi
import com.mision.app.presentation.celebrations.toCelebrationUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Editor sheet state: `null` in the UI state means the sheet is closed. */
data class MissionEditorState(
    val templateId: String? = null,
    val title: String = "",
    val description: String = "",
    val category: MissionCategory = MissionCategory.PERSONAL,
    val difficulty: MissionDifficulty = MissionDifficulty.FACIL,
    val durationMinutes: Int = 0,
    val isRecurring: Boolean = true,
    val titleError: String? = null,
    val confirmDelete: Boolean = false,
) {
    val isEditing: Boolean get() = templateId != null
}

data class MissionsUiState(
    val isLoading: Boolean = true,
    val missions: List<Mission> = emptyList(),
    val filter: MissionFilter = MissionFilter(),
    /** Category chips, favourites first. */
    val categories: List<MissionCategory> = MissionCategory.entries,
    val editor: MissionEditorState? = null,
) {
    val completedCount: Int get() = missions.count { it.isCompleted }
    val totalToday: Int get() = missions.size
    val visibleMissions: List<Mission> get() = missions.filter(filter::matches)
}

/** Missions screen: list, filters, search and custom mission CRUD. */
@OptIn(ExperimentalCoroutinesApi::class)
class MissionsViewModel(
    private val useCases: UseCases,
    settingsRepository: SettingsRepository,
    private val clock: ClockProvider,
) : ViewModel() {

    private val day = MutableStateFlow(clock.todayEpochDay())
    private val filter = MutableStateFlow(MissionFilter())
    private val editor = MutableStateFlow<MissionEditorState?>(null)
    private val _celebration = MutableStateFlow<CelebrationUi?>(null)

    val celebration: StateFlow<CelebrationUi?> = _celebration.asStateFlow()
    val messages = UserMessages()

    val uiState: StateFlow<MissionsUiState> = combine(
        day.flatMapLatest { useCases.getDailyMissions(it) },
        settingsRepository.settings.map { it.preferredCategories },
        filter,
        editor,
    ) { missions, favourites, filter, editor ->
        MissionsUiState(
            isLoading = false,
            missions = missions,
            filter = filter,
            categories = MissionCategory.entries.sortedBy { it !in favourites },
            editor = editor,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MissionsUiState(),
    )

    /** Moves to the new day after midnight and materialises its missions. */
    fun refresh() {
        viewModelScope.launch {
            runCatching {
                useCases.ensureDailyMissions()
                day.value = clock.todayEpochDay()
            }.onFailure { messages.show(GENERIC_ERROR) }
        }
    }

    // ---- Filters ---------------------------------------------------------
    fun onCategoryFilter(category: MissionCategory?) = filter.update { it.copy(category = category) }

    fun onStatusFilter(status: MissionStatus?) = filter.update { it.copy(status = status) }

    fun onSearch(query: String) = filter.update { it.copy(query = query) }

    fun clearFilters() {
        filter.value = MissionFilter()
    }

    // ---- Completion ------------------------------------------------------
    fun onToggleMission(mission: Mission) {
        viewModelScope.launch {
            runCatching {
                if (mission.isCompleted) {
                    useCases.uncompleteMission(mission.id)
                } else {
                    useCases.completeMission(mission.id)?.let { _celebration.value = it.toCelebrationUi() }
                }
            }.onFailure { messages.show(GENERIC_ERROR) }
        }
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    // ---- Editor ----------------------------------------------------------
    fun openEditor(mission: Mission? = null) {
        editor.value = if (mission == null) {
            MissionEditorState()
        } else {
            MissionEditorState(
                templateId = mission.templateId,
                title = mission.title,
                description = mission.description,
                category = mission.category,
                difficulty = mission.difficulty,
                durationMinutes = mission.durationMinutes ?: 0,
                isRecurring = mission.isRecurring,
            )
        }
    }

    fun closeEditor() {
        editor.value = null
    }

    fun onEditorTitle(value: String) =
        updateEditor { it.copy(title = value.take(MISSION_TITLE_MAX_LENGTH), titleError = null) }

    fun onEditorDescription(value: String) = updateEditor { it.copy(description = value) }
    fun onEditorCategory(value: MissionCategory) = updateEditor { it.copy(category = value) }
    fun onEditorDifficulty(value: MissionDifficulty) = updateEditor { it.copy(difficulty = value) }
    fun onEditorDuration(value: Int) = updateEditor { it.copy(durationMinutes = value) }
    fun onEditorRecurring(value: Boolean) = updateEditor { it.copy(isRecurring = value) }
    fun onEditorDeleteRequest(show: Boolean) = updateEditor { it.copy(confirmDelete = show) }

    private fun updateEditor(transform: (MissionEditorState) -> MissionEditorState) {
        editor.update { current -> current?.let(transform) }
    }

    fun saveEditor() {
        val current = editor.value ?: return
        viewModelScope.launch {
            val draft = MissionDraft(
                title = current.title.trim(),
                description = current.description.trim(),
                category = current.category,
                difficulty = current.difficulty,
                durationMinutes = current.durationMinutes.takeIf { it > 0 },
                isRecurring = current.isRecurring,
            )
            val result = runCatching {
                if (current.templateId == null) {
                    useCases.createCustomMission(draft)
                } else {
                    useCases.updateCustomMission(current.templateId, draft)
                }
            }.getOrElse { AppResult.Error(GENERIC_ERROR) }

            when (result) {
                is AppResult.Success -> {
                    editor.value = null
                    messages.show(if (current.isEditing) "Misión actualizada." else "Misión creada. Ya está en tu lista de hoy.")
                }
                is AppResult.Error -> updateEditor { it.copy(titleError = result.message) }
            }
        }
    }

    fun deleteEditorMission() {
        val id = editor.value?.templateId ?: return
        viewModelScope.launch {
            val deleted = runCatching { useCases.deleteCustomMission(id) }.getOrDefault(false)
            editor.value = null
            messages.show(if (deleted) "Misión eliminada." else GENERIC_ERROR)
        }
    }

    companion object {
        fun factory(
            useCases: UseCases,
            settingsRepository: SettingsRepository,
            clock: ClockProvider,
        ) = viewModelFactory {
            initializer {
                MissionsViewModel(
                    useCases = useCases,
                    settingsRepository = settingsRepository,
                    clock = clock,
                )
            }
        }
    }
}
