package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.domain.model.ProfileStats
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.usecase.AchievementProgress
import com.mision.app.domain.usecase.NAME_MAX_LENGTH
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile.empty(),
    /** Null until the first snapshot is loaded. */
    val stats: ProfileStats? = null,
    val achievements: List<AchievementProgress> = emptyList(),
    /** Non-null while the name editor is open; the value is the draft. */
    val nameDraft: String? = null,
)

/** Perfil screen: identity, progression, statistics and achievements. */
class ProfileViewModel(
    private val useCases: UseCases,
    progressRepository: ProgressRepository,
) : ViewModel() {

    private val stats = MutableStateFlow<ProfileStats?>(null)
    private val achievements = MutableStateFlow<List<AchievementProgress>>(emptyList())
    private val nameDraft = MutableStateFlow<String?>(null)

    val messages = UserMessages()

    val uiState: StateFlow<ProfileUiState> = combine(
        progressRepository.observeProfile(),
        stats,
        achievements,
        nameDraft,
    ) { profile, stats, achievements, draft ->
        ProfileUiState(profile = profile, stats = stats, achievements = achievements, nameDraft = draft)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    /** Statistics are snapshots: reloaded every time the screen is shown. */
    fun refresh() {
        viewModelScope.launch {
            runCatching {
                stats.value = useCases.getProfileStats()
                achievements.value = useCases.getAchievements()
            }.onFailure { messages.show(GENERIC_ERROR) }
        }
    }

    fun startEditName() {
        nameDraft.value = uiState.value.profile.name
    }

    fun onNameDraft(value: String) {
        nameDraft.value = value.take(NAME_MAX_LENGTH)
    }

    fun cancelEditName() {
        nameDraft.value = null
    }

    fun saveName() {
        val draft = nameDraft.value ?: return
        if (draft.isBlank()) {
            messages.show("Escribe un nombre.")
            return
        }
        viewModelScope.launch {
            runCatching { useCases.updateProfileName(draft) }
                .onSuccess {
                    nameDraft.value = null
                    messages.show("Nombre actualizado.")
                }
                .onFailure { messages.show(GENERIC_ERROR) }
        }
    }

    companion object {
        fun factory(useCases: UseCases, progressRepository: ProgressRepository) = viewModelFactory {
            initializer { ProfileViewModel(useCases = useCases, progressRepository = progressRepository) }
        }
    }
}
