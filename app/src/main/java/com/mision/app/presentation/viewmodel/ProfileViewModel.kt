package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.domain.model.Achievement
import com.mision.app.domain.model.AchievementStats
import com.mision.app.domain.model.ProfileStats
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.usecase.AchievementBoard
import com.mision.app.domain.usecase.UpdateProfileNameUseCase
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile.empty(),
    val stats: ProfileStats? = null,
    val achievements: List<Achievement> = emptyList(),
    val achievementStats: AchievementStats = AchievementStats(
        totalMissionsCompleted = 0,
        longestStreak = 0,
        perfectDays = 0,
        level = 1,
        coinsEarned = 0,
        purchases = 0,
    ),
    /** Non-null while the name editor is open; the value is the draft. */
    val nameDraft: String? = null,
    val infoMessage: String? = null,
) {
    val isEditingName: Boolean get() = nameDraft != null
}

/** Perfil screen: identity, progression, statistics and achievements. */
class ProfileViewModel(
    private val useCases: UseCases,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val _stats = MutableStateFlow<ProfileStats?>(null)
    private val _board = MutableStateFlow<AchievementBoard?>(null)
    private val _nameDraft = MutableStateFlow<String?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    val infoMessage: StateFlow<String?> = _infoMessage

    val uiState: StateFlow<ProfileUiState> = combine(
        progressRepository.observeProfile(),
        _stats,
        _board,
        _nameDraft,
        _infoMessage,
    ) { profile, stats, board, nameDraft, info ->
        ProfileUiState(
            isLoading = false,
            profile = profile,
            stats = stats,
            achievements = board?.achievements.orEmpty(),
            achievementStats = board?.stats ?: AchievementStats(
                totalMissionsCompleted = profile.totalMissionsCompleted,
                longestStreak = stats?.streak?.longestStreak ?: 0,
                perfectDays = 0,
                level = profile.level,
                coinsEarned = 0,
                purchases = 0,
            ),
            nameDraft = nameDraft,
            infoMessage = info,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                _stats.value = useCases.getProfileStats()
                _board.value = useCases.getAchievements()
            }
        }
    }

    fun startEditName() {
        _nameDraft.value = uiState.value.profile.name
    }

    fun onNameDraft(value: String) {
        _nameDraft.value = value.take(UpdateProfileNameUseCase.MAX_LENGTH)
    }

    fun cancelEditName() {
        _nameDraft.value = null
    }

    fun saveName() {
        val draft = _nameDraft.value ?: return
        viewModelScope.launch {
            runCatching {
                useCases.updateProfileName(draft)
                _stats.value = useCases.getProfileStats()
            }
            _nameDraft.value = null
            _infoMessage.value = "Nombre actualizado."
        }
    }

    fun dismissInfo() {
        _infoMessage.value = null
    }

    /** Current value of an achievement, for its progress bar. */
    fun currentValueFor(achievement: Achievement): Int =
        achievement.definition.currentValue(uiState.value.achievementStats)

    companion object {
        fun factory(useCases: UseCases, progressRepository: ProgressRepository) = viewModelFactory {
            initializer {
                ProfileViewModel(
                    useCases = useCases,
                    progressRepository = progressRepository,
                )
            }
        }
    }
}
