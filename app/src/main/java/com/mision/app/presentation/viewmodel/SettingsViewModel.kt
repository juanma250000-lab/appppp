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

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }

    fun setThemeMode(mode: ThemeMode) = launch { settingsRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = launch { settingsRepository.setDynamicColor(enabled) }
    fun setSoundEnabled(enabled: Boolean) = launch { settingsRepository.setSoundEnabled(enabled) }
    fun setAnimationsEnabled(enabled: Boolean) =
        launch { settingsRepository.setAnimationsEnabled(enabled) }

    fun setNotificationsEnabled(enabled: Boolean) =
        launch { settingsRepository.setNotificationsEnabled(enabled) }

    fun setReminderTime(hour: Int, minute: Int) =
        launch { settingsRepository.setReminderTime(hour, minute) }

    fun setPreferredCategories(categories: Set<MissionCategory>) =
        launch { settingsRepository.setPreferredCategories(categories) }

    fun markNotificationPermissionRequested() =
        launch { settingsRepository.markNotificationPermissionRequested() }

    /** Full reset of the progression (keeps the preferences). */
    fun resetProgress() = launch { useCases.resetProgress() }

    companion object {
        fun factory(settingsRepository: SettingsRepository, useCases: UseCases) = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    useCases = useCases,
                )
            }
        }
    }
}
