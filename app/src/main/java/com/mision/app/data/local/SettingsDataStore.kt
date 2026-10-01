package com.mision.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mision.app.domain.model.AppSettings
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.misionDataStore: DataStore<Preferences> by preferencesDataStore(name = "mision_settings")

/**
 * Lightweight preferences (onboarding, theme, notifications). Structured game
 * data lives in Room; everything the user merely configures lives here.
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val PREFERRED_CATEGORIES = stringSetPreferencesKey("preferred_categories")
        val PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
    }

    val settings: Flow<AppSettings> = context.misionDataStore.data.map { prefs ->
        AppSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 20,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true,
            preferredCategories = prefs[Keys.PREFERRED_CATEGORIES]
                ?.mapNotNull { name -> runCatching { MissionCategory.valueOf(name) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            notificationPermissionRequested = prefs[Keys.PERMISSION_REQUESTED] ?: false,
        )
    }

    suspend fun setOnboardingCompleted(value: Boolean) = edit { it[Keys.ONBOARDING_COMPLETED] = value }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setDynamicColor(value: Boolean) = edit { it[Keys.DYNAMIC_COLOR] = value }

    suspend fun setNotificationsEnabled(value: Boolean) = edit { it[Keys.NOTIFICATIONS_ENABLED] = value }

    suspend fun setReminderTime(hour: Int, minute: Int) = edit {
        it[Keys.REMINDER_HOUR] = hour
        it[Keys.REMINDER_MINUTE] = minute
    }

    suspend fun setSoundEnabled(value: Boolean) = edit { it[Keys.SOUND_ENABLED] = value }

    suspend fun setAnimationsEnabled(value: Boolean) = edit { it[Keys.ANIMATIONS_ENABLED] = value }

    suspend fun setPreferredCategories(categories: Set<MissionCategory>) = edit {
        it[Keys.PREFERRED_CATEGORIES] = categories.map { category -> category.name }.toSet()
    }

    suspend fun setNotificationPermissionRequested(value: Boolean) = edit {
        it[Keys.PERMISSION_REQUESTED] = value
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.misionDataStore.edit(block)
    }
}
