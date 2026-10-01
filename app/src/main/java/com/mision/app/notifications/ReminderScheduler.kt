package com.mision.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mision.app.core.time.ClockProvider
import com.mision.app.core.time.SystemClockProvider
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily reminder with WorkManager (no exact alarms, no battery
 * complaints). The worker re-schedules itself for the next day, and settings
 * changes simply replace the pending request.
 */
class ReminderScheduler(
    private val context: Context,
    private val clock: ClockProvider = SystemClockProvider(),
) {

    fun schedule(hour: Int, minute: Int, enabled: Boolean) {
        val workManager = runCatching { WorkManager.getInstance(context) }.getOrNull() ?: return
        if (!enabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK)
            return
        }
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillisUntil(hour, minute), TimeUnit.MILLISECONDS)
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    /** Milliseconds from now until the next occurrence of [hour]:[minute]. */
    fun delayMillisUntil(hour: Int, minute: Int, now: LocalDateTime = clock.now()): Long {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        var target = LocalDateTime.of(now.toLocalDate(), LocalTime.of(safeHour, safeMinute))
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis().coerceAtLeast(MIN_DELAY_MS)
    }

    companion object {
        const val UNIQUE_WORK = "mision_recordatorio_diario"
        const val TAG = "recordatorio"
        private const val MIN_DELAY_MS = 30_000L
    }
}
