package com.mision.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import com.mision.app.MisionApp
import com.mision.app.data.repository.MissionRepositoryImpl
import kotlinx.coroutines.flow.first

/**
 * Fires one mission's reminder. It only notifies when reminders are enabled in
 * Ajustes and the mission is still pending today, then queues tomorrow's
 * occurrence while the mission keeps its reminder.
 */
class MissionReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? MisionApp)?.container ?: return Result.success()
        val templateId = inputData.getString(MissionReminderScheduler.KEY_TEMPLATE_ID)
            ?: return Result.success()
        val missions = container.missionRepository

        // Deleted mission or reminder switched off: nothing to do, nothing to re-queue.
        val reminder = runCatching { missions.remindersToSchedule() }.getOrNull()
            ?.firstOrNull { it.templateId == templateId }
            ?: return Result.success()

        val settings = container.settingsRepository.settings.first()
        if (settings.notificationsEnabled) {
            val today = container.clock.todayEpochDay()
            val mission = runCatching {
                // The app may not have been opened today yet.
                missions.ensureDailyMissions(today)
                missions.getMission(MissionRepositoryImpl.instanceId(templateId, today))
            }.getOrNull()
            if (mission != null && !mission.isCompleted) {
                container.notifier.showMissionReminder(
                    templateId = templateId,
                    title = mission.title,
                    xpReward = mission.xpReward,
                )
            }
        }

        container.missionReminderScheduler.schedule(reminder, ExistingWorkPolicy.APPEND_OR_REPLACE)
        return Result.success()
    }
}
