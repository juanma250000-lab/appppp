package com.mision.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mision.app.data.enumValueOr
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
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val PREFERRED_CATEGORIES = stringSetPreferencesKey("preferred_categories")
    }

    private val defaults = AppSettings()

    val settings: Flow<AppSettings> = context.misionDataStore.data.map { prefs ->
        AppSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: defaults.onboardingCompleted,
            themeMode = prefs[Keys.THEME_MODE]?.let { enumValueOr(it, defaults.themeMode) }
                ?: defaults.themeMode,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: defaults.dynamicColor,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: defaults.notificationsEnabled,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: defaults.reminderHour,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: defaults.reminderMinute,
            animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: defaults.animationsEnabled,
            preferredCategories = prefs[Keys.PREFERRED_CATEGORIES]
                ?.mapNotNull { name -> MissionCategory.entries.firstOrNull { it.name == name } }
                ?.toSet()
                ?: defaults.preferredCategories,
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

    suspend fun setAnimationsEnabled(value: Boolean) = edit { it[Keys.ANIMATIONS_ENABLED] = value }

    suspend fun setPreferredCategories(categories: Set<MissionCategory>) = edit {
        it[Keys.PREFERRED_CATEGORIES] = categories.map { category -> category.name }.toSet()
    }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        context.misionDataStore.edit(block)
    }
}
