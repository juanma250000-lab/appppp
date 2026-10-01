package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.usecase.SetPreferredCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Steps of the first-run experience, in the order they are shown. */
enum class OnboardingStep(val dotIndex: Int) {
    INTRO_ONE(0),
    INTRO_TWO(0),
    INTRO_THREE(0),
    YOUR_NAME(1),
    PET_NAME(1),
    CATEGORIES(2),
    PERMISSIONS(2),
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.INTRO_ONE,
    val userName: String = "",
    val petName: String = "",
    val categories: Set<MissionCategory> = emptySet(),
    val permissionRequested: Boolean = false,
    val isFinishing: Boolean = false,
) {
    val isFirstStep: Boolean get() = step == OnboardingStep.INTRO_ONE
    val isLastStep: Boolean get() = step == OnboardingStep.PERMISSIONS
    val canContinue: Boolean
        get() = when (step) {
            OnboardingStep.YOUR_NAME -> userName.isNotBlank()
            OnboardingStep.PET_NAME -> petName.isNotBlank()
            OnboardingStep.CATEGORIES -> categories.isNotEmpty()
            else -> true
        }
}

/** Onboarding: 3 intro screens + name, pet, categories and permission. */
class OnboardingViewModel(
    private val progressRepository: ProgressRepository,
    private val petRepository: PetRepository,
    private val settingsRepository: SettingsRepository,
    private val setPreferredCategories: SetPreferredCategoriesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun next() {
        _state.update { current ->
            if (!current.canContinue) return@update current
            val next = steps.getOrNull(steps.indexOf(current.step) + 1) ?: current.step
            current.copy(step = next)
        }
    }

    fun back() {
        _state.update { current ->
            val index = steps.indexOf(current.step)
            current.copy(step = steps.getOrNull(index - 1) ?: current.step)
        }
    }

    fun onUserName(value: String) {
        _state.update { it.copy(userName = value.trim().take(NAME_MAX_LENGTH)) }
    }

    fun onPetName(value: String) {
        _state.update { it.copy(petName = value.trim().take(NAME_MAX_LENGTH)) }
    }

    fun toggleCategory(category: MissionCategory) {
        _state.update { current ->
            val updated = if (category in current.categories) {
                current.categories - category
            } else {
                current.categories + category
            }
            current.copy(categories = updated)
        }
    }

    fun onPermissionRequested() {
        _state.update { it.copy(permissionRequested = true) }
    }

    /** Persists everything and marks onboarding as completed. */
    fun finish(onDone: () -> Unit) {
        if (_state.value.isFinishing) return
        _state.update { it.copy(isFinishing = true) }
        viewModelScope.launch {
            runCatching {
                val current = _state.value
                val profile = progressRepository.getProfile()
                if (current.userName.isNotBlank()) {
                    progressRepository.saveProfile(profile.copy(name = current.userName))
                }
                val pet = petRepository.getPet()
                petRepository.savePet(pet.copy(name = current.petName.ifBlank { pet.name }))
                setPreferredCategories(current.categories)
                settingsRepository.markNotificationPermissionRequested()
                settingsRepository.setOnboardingCompleted(true)
            }
            onDone()
        }
    }

    companion object {
        private const val NAME_MAX_LENGTH = 24

        private val steps = listOf(
            OnboardingStep.INTRO_ONE,
            OnboardingStep.INTRO_TWO,
            OnboardingStep.INTRO_THREE,
            OnboardingStep.YOUR_NAME,
            OnboardingStep.PET_NAME,
            OnboardingStep.CATEGORIES,
            OnboardingStep.PERMISSIONS,
        )

        fun factory(
            progressRepository: ProgressRepository,
            petRepository: PetRepository,
            settingsRepository: SettingsRepository,
            setPreferredCategories: SetPreferredCategoriesUseCase,
        ) = viewModelFactory {
            initializer {
                OnboardingViewModel(
                    progressRepository = progressRepository,
                    petRepository = petRepository,
                    settingsRepository = settingsRepository,
                    setPreferredCategories = setPreferredCategories,
                )
            }
        }
    }
}
