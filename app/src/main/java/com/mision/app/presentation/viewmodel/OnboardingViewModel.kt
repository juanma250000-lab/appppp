package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.usecase.CompleteOnboardingUseCase
import com.mision.app.domain.usecase.NAME_MAX_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Steps of the first-run experience, in the order they are shown. */
enum class OnboardingStep {
    INTRO_ONE,
    INTRO_TWO,
    INTRO_THREE,
    YOUR_NAME,
    PET_NAME,
    CATEGORIES,
    PERMISSIONS,
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.INTRO_ONE,
    val userName: String = "",
    val petName: String = "",
    val categories: Set<MissionCategory> = emptySet(),
    val permissionRequested: Boolean = false,
    val isFinishing: Boolean = false,
) {
    val isFirstStep: Boolean get() = step == OnboardingStep.entries.first()
    val isLastStep: Boolean get() = step == OnboardingStep.entries.last()
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
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun next() = _state.update { current ->
        if (!current.canContinue) return@update current
        val next = OnboardingStep.entries.getOrNull(current.step.ordinal + 1) ?: current.step
        current.copy(step = next)
    }

    fun back() = _state.update { current ->
        current.copy(step = OnboardingStep.entries.getOrNull(current.step.ordinal - 1) ?: current.step)
    }

    // Names are kept as typed (spaces included); they are trimmed when saved.
    fun onUserName(value: String) = _state.update { it.copy(userName = value.take(NAME_MAX_LENGTH)) }

    fun onPetName(value: String) = _state.update { it.copy(petName = value.take(NAME_MAX_LENGTH)) }

    fun toggleCategory(category: MissionCategory) = _state.update { current ->
        val updated = if (category in current.categories) current.categories - category else current.categories + category
        current.copy(categories = updated)
    }

    fun onPermissionRequested() = _state.update { it.copy(permissionRequested = true) }

    /** Persists everything and marks onboarding as completed. */
    fun finish(onDone: () -> Unit) {
        if (_state.value.isFinishing) return
        _state.update { it.copy(isFinishing = true) }
        val current = _state.value
        viewModelScope.launch {
            // Even if saving the names fails the user must not be stuck here:
            // defaults are used and the app opens normally.
            runCatching { completeOnboarding(current.userName, current.petName, current.categories) }
            onDone()
        }
    }

    companion object {
        fun factory(completeOnboarding: CompleteOnboardingUseCase) = viewModelFactory {
            initializer { OnboardingViewModel(completeOnboarding) }
        }
    }
}
