package com.mision.app

import android.app.Application
import com.mision.app.notifications.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application entry point.
 *
 * Owns the object graph (composition root). All dependencies are created once
 * here and exposed through [AppContainer]; ViewModels never touch Room,
 * DataStore or WorkManager directly.
 */
class MisionApp : Application() {

    lateinit var container: AppContainer
        private set

    /** Runs the one-off start-up work off the main thread. */
    private val bootstrapScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Notification channels must exist before the first post: on Android 8+
        // a notification sent to an unknown channel is dropped silently.
        NotificationChannels.ensure(this)
        container = AppContainer(this)
        armSavedReminder()
    }

    /**
     * Re-arms the daily reminder with the settings already on disk.
     *
     * WorkManager survives process death and reboots, so this only matters on
     * a fresh install (or after clearing app data), where nothing is queued
     * yet and the default 20:00 reminder would otherwise never fire.
     */
    private fun armSavedReminder() {
        bootstrapScope.launch {
            runCatching {
                val settings = container.settingsRepository.settings.first()
                container.reminderScheduler.schedule(
                    hour = settings.reminderHour,
                    minute = settings.reminderMinute,
                    enabled = settings.notificationsEnabled,
                )
            }
        }
    }
}
