package com.mision.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mision.app.domain.model.AppSettings
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.ThemeMode
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.usecase.UseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Ajustes screen: preferences, reminders and the progress reset. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val useCases: UseCases,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    val messages = UserMessages()

    private fun update(successMessage: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { successMessage?.let(messages::show) }
                .onFailure { messages.show(GENERIC_ERROR) }
        }
    }

    fun setThemeMode(mode: ThemeMode) = update { settingsRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = update { settingsRepository.setDynamicColor(enabled) }
    fun setAnimationsEnabled(enabled: Boolean) = update { settingsRepository.setAnimationsEnabled(enabled) }

    fun setNotificationsEnabled(enabled: Boolean) = update { settingsRepository.setNotificationsEnabled(enabled) }

    /** The system refused the notification permission: keep the reminder off and say why. */
    fun onNotificationPermissionDenied() = update(
        successMessage = "Sin permiso de notificaciones no podemos avisarte. Puedes activarlo en los ajustes del sistema.",
    ) { settingsRepository.setNotificationsEnabled(false) }

    fun setReminderTime(hour: Int, minute: Int) = update { settingsRepository.setReminderTime(hour, minute) }

    fun toggleFavourite(category: MissionCategory) = update {
        val current = settings.value.preferredCategories
        settingsRepository.setPreferredCategories(
            if (category in current) current - category else current + category,
        )
    }

    /** Full reset of the progression (keeps the preferences). */
    fun resetProgress() = update(successMessage = "Progreso restablecido. ¡A por un nuevo comienzo!") {
        useCases.resetProgress()
    }

    companion object {
        fun factory(settingsRepository: SettingsRepository, useCases: UseCases) = viewModelFactory {
            initializer { SettingsViewModel(settingsRepository = settingsRepository, useCases = useCases) }
        }
    }
}
