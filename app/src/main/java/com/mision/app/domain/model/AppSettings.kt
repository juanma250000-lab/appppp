package com.mision.app.domain.model

/** Theme selection strategy. */
enum class ThemeMode(val displayName: String) {
    SYSTEM("Automático"),
    LIGHT("Claro"),
    DARK("Oscuro"),
}

/** Lightweight, non-sensitive preferences persisted with DataStore. */
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = DEFAULT_REMINDER_HOUR,
    val reminderMinute: Int = 0,
    val animationsEnabled: Boolean = true,
    /** Missions of these categories are listed first in the daily list. */
    val preferredCategories: Set<MissionCategory> = emptySet(),
) {
    companion object {
        const val DEFAULT_REMINDER_HOUR = 20
    }
}

/** User-facing result of an action, always with a Spanish message. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Error(val message: String) : AppResult<Nothing>
}
