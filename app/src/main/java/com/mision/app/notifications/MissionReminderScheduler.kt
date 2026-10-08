package com.mision.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mision.app.domain.repository.MissionReminder
import com.mision.app.domain.repository.MissionRepository
import java.util.concurrent.TimeUnit

/**
 * Schedules the per-mission reminders: one WorkManager request per mission,
 * which fires at the mission's own time and then re-schedules itself for the
 * next day (same pattern as the daily reminder).
 */
class MissionReminderScheduler(
    private val context: Context,
    private val timing: ReminderScheduler,
) {

    /**
     * Brings the queued reminders in line with the missions.
     *
     * @param replaceExisting true after the user changes missions: everything
     * is re-queued with the new times. At app start it is false and existing
     * requests are kept, because the process may have been started precisely
     * to run one of them and replacing it would push it to tomorrow.
     */
    suspend fun syncAll(missionRepository: MissionRepository, replaceExisting: Boolean = true) {
        val workManager = runCatching { WorkManager.getInstance(context) }.getOrNull() ?: return
        val reminders = runCatching { missionRepository.remindersToSchedule() }.getOrNull() ?: return
        if (replaceExisting) {
            // WorkManager runs operations in order, so the cancel lands before the new requests.
            workManager.cancelAllWorkByTag(TAG)
            reminders.forEach { schedule(it) }
        } else {
            // Reminders of deleted missions end on their own: the worker finds no mission.
            reminders.forEach { schedule(it, ExistingWorkPolicy.KEEP) }
        }
    }

    /**
     * Queues the next occurrence of one mission reminder. The worker itself
     * passes [ExistingWorkPolicy.APPEND_OR_REPLACE]: REPLACE would cancel the
     * very worker that is running.
     */
    fun schedule(
        reminder: MissionReminder,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE,
    ) {
        val workManager = runCatching { WorkManager.getInstance(context) }.getOrNull() ?: return
        val request = OneTimeWorkRequestBuilder<MissionReminderWorker>()
            .setInitialDelay(timing.delayMillisUntil(reminder.hour, reminder.minute), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_TEMPLATE_ID to reminder.templateId))
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(uniqueName(reminder.templateId), policy, request)
    }

    companion object {
        const val TAG = "recordatorio_mision"
        const val KEY_TEMPLATE_ID = "templateId"

        private fun uniqueName(templateId: String) = "mision_aviso_$templateId"
    }
}
