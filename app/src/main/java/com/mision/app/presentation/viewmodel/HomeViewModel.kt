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
import com.mision.app.presentation.celebrations.toCelebrationUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the home screen renders. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile.empty(),
    val streak: StreakState = StreakState.empty(),
    val pet: Pet = Pet.default(epochDay = 0),
    val missions: List<Mission> = emptyList(),
    val greeting: String = "",
    val dateLabel: String = "",
    val motivationalLine: String = "",
) {
    val completedToday: Int get() = missions.count { it.isCompleted }
    val totalToday: Int get() = missions.size
    val progress: Float get() = if (totalToday == 0) 0f else completedToday.toFloat() / totalToday
    val allCompleted: Boolean get() = totalToday > 0 && completedToday == totalToday
    val speech: String get() = PetSpeechProvider.forDailyProgress(progress)
}

/**
 * Home screen logic: today's missions, progression, streak, pet speech and
 * the completion orchestration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val useCases: UseCases,
    progressRepository: ProgressRepository,
    gamificationRepository: GamificationRepository,
    petRepository: PetRepository,
    private val clock: ClockProvider,
) : ViewModel() {

    /** The day on screen; moves forward on resume after midnight. */
    private val day = MutableStateFlow(clock.todayEpochDay())

    private val _celebration = MutableStateFlow<CelebrationUi?>(null)
    val celebration: StateFlow<CelebrationUi?> = _celebration.asStateFlow()

    val messages = UserMessages()

    private var busy = false

    val uiState: StateFlow<HomeUiState> = combine(
        day.flatMapLatest { useCases.getDailyMissions(it) },
        progressRepository.observeProfile(),
        gamificationRepository.observeStreak(),
        petRepository.observePet(),
    ) { missions, profile, streak, pet ->
        val now = clock.now()
        HomeUiState(
            isLoading = false,
            profile = profile,
            streak = streak,
            pet = pet,
            missions = missions,
            greeting = DateFormats.greeting(now.hour),
            dateLabel = DateFormats.longDate(now.toLocalDate()),
            motivationalLine = PetSpeechProvider.motivationalLine(day.value),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /** Re-syncs the day (missions, streak, pet mood) when the screen resumes. */
    fun refresh() {
        viewModelScope.launch {
            runCatching {
                useCases.ensureDailyMissions()
                day.value = clock.todayEpochDay()
                useCases.calculateStreak()
                useCases.updatePetMood()
            }.onFailure { messages.show(GENERIC_ERROR) }
        }
    }

    /** Completes or reverts a mission and raises the proper celebration. */
    fun onToggleMission(mission: Mission) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            runCatching {
                if (mission.isCompleted) {
                    useCases.uncompleteMission(mission.id)
                } else {
                    useCases.completeMission(mission.id)?.let { result ->
                        _celebration.value = result.toCelebrationUi()
                    }
                }
            }.onFailure { messages.show(GENERIC_ERROR) }
            busy = false
        }
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    companion object {
        fun factory(
            useCases: UseCases,
            progressRepository: ProgressRepository,
            gamificationRepository: GamificationRepository,
            petRepository: PetRepository,
            clock: ClockProvider,
        ) = viewModelFactory {
            initializer {
                HomeViewModel(
                    useCases = useCases,
                    progressRepository = progressRepository,
                    gamificationRepository = gamificationRepository,
                    petRepository = petRepository,
                    clock = clock,
                )
            }
        }
    }
}
