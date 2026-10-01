package com.mision.app.data.repository

import com.mision.app.data.local.SettingsDataStore
import com.mision.app.domain.model.AppSettings
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.ThemeMode
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Settings repository. Writing notification settings also reschedules the
 * daily reminder so the UI never has to know about WorkManager.
 */
class SettingsRepositoryImpl(
    private val store: SettingsDataStore,
    private val scheduler: ReminderScheduler,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = store.settings

    override suspend fun setOnboardingCompleted(value: Boolean) =
        store.setOnboardingCompleted(value)

    override suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)

    override suspend fun setDynamicColor(value: Boolean) = store.setDynamicColor(value)

    override suspend fun setNotificationsEnabled(value: Boolean) {
        store.setNotificationsEnabled(value)
        val current = store.settings.first()
        scheduler.schedule(current.reminderHour, current.reminderMinute, value)
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        store.setReminderTime(hour, minute)
        val current = store.settings.first()
        scheduler.schedule(hour, minute, current.notificationsEnabled)
    }

    override suspend fun setAnimationsEnabled(value: Boolean) = store.setAnimationsEnabled(value)

    override suspend fun setPreferredCategories(categories: Set<MissionCategory>) =
        store.setPreferredCategories(categories)
}
