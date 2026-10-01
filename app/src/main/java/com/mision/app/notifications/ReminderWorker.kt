package com.mision.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mision.app.MisionApp
import kotlinx.coroutines.flow.first

/**
 * Fires the daily reminder and immediately re-schedules itself for the next
 * day, which keeps the reminder aligned with the configured time even after
 * the clock or the time zone changes.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? MisionApp)?.container ?: return Result.success()
        val settings = container.settingsRepository.settings.first()

        if (settings.notificationsEnabled) {
            val pending = runCatching {
                // If the app was not opened today the day's missions do not
                // exist yet; without this every reminder would read "all done".
                val today = container.clock.todayEpochDay()
                container.missionRepository.ensureDailyMissions(today)
                container.missionRepository.pendingCountToday(today)
            }.getOrNull()

            container.notifier.showDailyReminder(
                pendingMissions = pending,
                hour = settings.reminderHour,
            )
        }

        container.reminderScheduler.schedule(
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            enabled = settings.notificationsEnabled,
        )
        return Result.success()
    }
}
