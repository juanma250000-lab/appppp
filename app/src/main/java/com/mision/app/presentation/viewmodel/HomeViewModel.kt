package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.core.gamification.PetSpeechProvider
import com.mision.app.core.time.ClockProvider
import com.mision.app.core.time.DateFormats
import com.mision.app.domain.model.Mission
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
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

/** Everything the home screen renders, assembled from a single combine. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile.empty(),
    val streak: StreakState = StreakState.empty(),
    val pet: Pet = Pet.default(name = "Nube", epochDay = 0),
    val missions: List<Mission> = emptyList(),
    val completedToday: Int = 0,
    val totalToday: Int = 0,
    val progress: Float = 0f,
    val speech: String = "",
    val greeting: String = "",
    val dateLabel: String = "",
    val motivationalLine: String = "",
    val celebration: CelebrationUi? = null,
    val isBusy: Boolean = false,
) {
    val allCompleted: Boolean get() = totalToday > 0 && completedToday >= totalToday
}

/**
 * Home screen logic: today's missions, progression, streak, pet speech and
 * the completion orchestration.
 */
class HomeViewModel(
    private val useCases: UseCases,
    private val progressRepository: ProgressRepository,
    private val gamificationRepository: GamificationRepository,
    private val petRepository: PetRepository,
    private val clock: ClockProvider,
    private val celebrationDispatcher: CelebrationDispatcher,
) : ViewModel() {

    private val epochDay: Int = clock.todayEpochDay()

    private val _celebration = MutableStateFlow<CelebrationUi?>(null)
    val celebration: StateFlow<CelebrationUi?> = _celebration.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        useCases.getDailyMissions(epochDay),
        progressRepository.observeProfile(),
        gamificationRepository.observeStreak(),
        petRepository.observePet(),
        _celebration,
    ) { missions, profile, streak, pet, celebration ->
        val completed = missions.count { it.isCompleted }
        val total = missions.size
        val ratio = if (total <= 0) 0f else completed.toFloat() / total
        val speechState = PetSpeechProvider.stateForProgress(ratio)
        val message = PetSpeechProvider.message(
            state = speechState,
            progressPercent = if (total <= 0) 0 else (ratio * 100).toInt(),
            petName = pet.name,
            streakDays = streak.currentStreak,
        )
        val now = clock.now()
        HomeUiState(
            isLoading = false,
            profile = profile,
            streak = streak,
            pet = pet,
            missions = missions,
            completedToday = completed,
            totalToday = total,
            progress = ratio,
            speech = celebration?.let { "${it.emoji} ${it.title}" } ?: message.text,
            greeting = DateFormats.greeting(now.hour),
            dateLabel = DateFormats.longDate(now.toLocalDate()),
            motivationalLine = PetSpeechProvider.motivationalLine(epochDay),
            celebration = celebration,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch {
            runCatching {
                useCases.ensureDailyMissions()
                useCases.calculateStreak()
                useCases.updatePetMood()
            }
        }
    }

    /** Completes or reverts a mission and raises the proper celebration. */
    fun onToggleMission(mission: Mission) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            try {
                if (mission.isCompleted) {
                    useCases.uncompleteMission(mission.id)
                } else {
                    val result = useCases.completeMission(mission.id) ?: return@launch
                    celebrationDispatcher.dispatch(result)
                    _celebration.value = result.toCelebrationUi()
                }
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    /** Re-syncs the day when the app returns to the foreground. */
    fun refresh() {
        viewModelScope.launch {
            runCatching {
                useCases.ensureDailyMissions()
                useCases.calculateStreak()
                useCases.updatePetMood()
            }
        }
    }

    companion object {
        fun factory(
            useCases: UseCases,
            progressRepository: ProgressRepository,
            gamificationRepository: GamificationRepository,
            petRepository: PetRepository,
            clock: ClockProvider,
            celebrationDispatcher: CelebrationDispatcher,
        ) = viewModelFactory {
            initializer {
                HomeViewModel(
                    useCases = useCases,
                    progressRepository = progressRepository,
                    gamificationRepository = gamificationRepository,
                    petRepository = petRepository,
                    clock = clock,
                    celebrationDispatcher = celebrationDispatcher,
                )
            }
        }
    }
}
