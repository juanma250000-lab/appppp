package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.core.gamification.PetSpeechProvider
import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.CosmeticSlot
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.usecase.PetAction
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PetUiState(
    val isLoading: Boolean = true,
    val pet: Pet = Pet.default(epochDay = 0),
    val speech: String = "",
    /** What the pet is wearing, in shop order. */
    val wearing: List<ShopItem> = emptyList(),
)

/** Mascota screen: mood, stats, actions and contextual speech. */
class PetViewModel(
    private val useCases: UseCases,
    petRepository: PetRepository,
    private val clock: ClockProvider,
) : ViewModel() {

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    val messages = UserMessages()

    val uiState: StateFlow<PetUiState> = petRepository.observePet()
        .map { pet ->
            PetUiState(
                isLoading = false,
                pet = pet,
                speech = PetSpeechProvider.forPetState(pet, clock.todayEpochDay()),
                wearing = CosmeticSlot.entries.mapNotNull { slot ->
                    pet.equipped.idFor(slot)?.let(ShopCatalog::byId)
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PetUiState(),
        )

    /** Inactivity is reflected every time the screen becomes visible. */
    fun refreshMood() {
        viewModelScope.launch { runCatching { useCases.updatePetMood() } }
    }

    fun interact(action: PetAction) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            runCatching { useCases.interactWithPet(action) }
                .onSuccess { messages.show(it.message) }
                .onFailure { messages.show(GENERIC_ERROR) }
            _busy.value = false
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
