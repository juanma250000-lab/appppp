package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.core.gamification.PetSpeechProvider
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.Pet
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.usecase.PetAction
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PetUiState(
    val isLoading: Boolean = true,
    val pet: Pet = Pet.default(name = "Nube", epochDay = 0),
    val speech: String = "",
    val infoMessage: String? = null,
    val busy: Boolean = false,
)

/** Mascota screen: mood, stats, actions and contextual speech. */
class PetViewModel(
    private val useCases: UseCases,
    private val petRepository: PetRepository,
    private val clock: ClockProvider,
) : ViewModel() {

    private val _infoMessage = MutableStateFlow<String?>(null)
    private val _busy = MutableStateFlow(false)

    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val epochDay: Int = clock.todayEpochDay()

    val uiState: StateFlow<PetUiState> = combine(
        petRepository.observePet(),
        _infoMessage,
        _busy,
    ) { pet, message, busy ->
        PetUiState(
            isLoading = false,
            pet = pet,
            speech = defaultSpeech(pet),
            infoMessage = message,
            busy = busy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PetUiState(),
    )


    fun interact(action: PetAction) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                val result = useCases.interactWithPet(action, uiState.value.pet.name)
                _infoMessage.value = result.message
            } finally {
                _busy.value = false
            }
        }
    }

    /** Recomputes the mood from recent activity; called whenever the screen is shown. */
    fun refreshMood() {
        viewModelScope.launch { runCatching { useCases.updatePetMood() } }
    }

    fun dismissInfo() {
        _infoMessage.value = null
    }

    private fun defaultSpeech(pet: Pet): String {
        val inactiveDays = (epochDay - pet.lastInteractionEpochDay).coerceAtLeast(0)
        return when {
            inactiveDays >= 6 -> "¡Te he echado de menos! Qué alegría verte."
            pet.energy <= 25 -> "Estoy un poco cansado. ¿Me dejas descansar?"
            pet.happiness >= 75 -> "¡Estoy feliz! Formamos un gran equipo."
            else -> PetSpeechProvider.motivationalLine(pet.happiness + epochDay)
        }
    }

    companion object {
        fun factory(
            useCases: UseCases,
            petRepository: PetRepository,
            clock: ClockProvider,
        ) = viewModelFactory {
            initializer {
                PetViewModel(
                    useCases = useCases,
                    petRepository = petRepository,
                    clock = clock,
                )
            }
        }
    }
}
