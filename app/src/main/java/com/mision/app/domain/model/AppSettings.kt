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
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val soundEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val preferredCategories: Set<MissionCategory> = emptySet(),
    val notificationPermissionRequested: Boolean = false,
) {
    val reminderLabel: String
        get() = "%02d:%02d".format(reminderHour, reminderMinute)
}

/** User-facing result of an action, always with a Spanish message. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Error(val message: String) : AppResult<Nothing>
}
