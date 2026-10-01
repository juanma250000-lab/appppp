package com.mision.app.data.repository

import com.mision.app.core.time.ClockProvider
import com.mision.app.data.SeedData
import com.mision.app.data.local.MissionDao
import com.mision.app.data.local.MissionInstanceEntity
import com.mision.app.data.toMission
import com.mision.app.domain.model.Mission
import com.mision.app.domain.repository.MissionDraft
import com.mision.app.domain.repository.MissionRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class MissionRepositoryImpl(
    private val dao: MissionDao,
    private val clock: ClockProvider,
) : MissionRepository {

    override fun observeMissionsForDay(epochDay: Int): Flow<List<Mission>> =
        combine(
            dao.observeInstancesForDay(epochDay),
            dao.observeActiveTemplates(),
        ) { instances, templates ->
            val byTemplate = templates.associateBy { it.id }
            instances.mapNotNull { instance ->
                byTemplate[instance.templateId]?.toMission(instance)
            }.sortedWith(
                compareBy<Mission> { it.isCompleted }
                    .thenBy { it.sortOrder }
                    .thenBy { it.title.lowercase() },
            )
        }

    override suspend fun getMission(id: String): Mission? {
        val instance = dao.getInstance(id) ?: return null
        val template = dao.getTemplate(instance.templateId) ?: return null
        return template.toMission(instance)
    }

    override suspend fun ensureDailyMissions(todayEpochDay: Int) {
        var templates = dao.getTemplates()
        if (templates.isEmpty()) {
            dao.upsertTemplates(SeedData.defaultTemplates(todayEpochDay))
            templates = dao.getTemplates()
        }

        val existing = dao.getInstancesForDay(todayEpochDay).map { it.templateId }.toSet()
        val missing = templates
            .filter { it.isActive }
            .filter { it.isRecurring || it.createdAtEpochDay == todayEpochDay }
            .filter { it.id !in existing }
            .map { template ->
                MissionInstanceEntity(
                    id = instanceId(template.id, todayEpochDay),
                    templateId = template.id,
                    dueEpochDay = todayEpochDay,
                    isCompleted = false,
                    completedAtEpochSecond = 0L,
                    createdAtEpochDay = todayEpochDay,
                )
            }
        if (missing.isNotEmpty()) dao.insertInstances(missing)

        dao.pruneInstancesBefore(todayEpochDay - KEEP_DAYS)
    }

    override suspend fun setCompleted(id: String, completed: Boolean, atEpochSecond: Long): Mission? {
        val instance = dao.getInstance(id) ?: return null
        val updated = instance.copy(
            isCompleted = completed,
            completedAtEpochSecond = if (completed) atEpochSecond else 0L,
        )
        dao.updateInstance(updated)
        val template = dao.getTemplate(updated.templateId) ?: return null
        return template.toMission(updated)
    }

    override suspend fun addCustomMission(draft: MissionDraft, todayEpochDay: Int): Mission {
        val templateId = "custom_${UUID.randomUUID()}"
        val template = SeedData.template(
            id = templateId,
            title = draft.title.trim(),
            description = draft.description.trim(),
            category = draft.category,
            difficulty = draft.difficulty,
            sortOrder = CUSTOM_ORDER,
            durationMinutes = draft.durationMinutes ?: 0,
            reminderHour = if (draft.reminderEnabled) draft.reminderHour else SeedData.REMINDER_OFF,
            reminderMinute = draft.reminderMinute,
            isRecurring = draft.isRecurring,
            isCustom = true,
            createdAt = todayEpochDay,
        )
        dao.upsertTemplate(template)
        val instance = MissionInstanceEntity(
            id = instanceId(templateId, todayEpochDay),
            templateId = templateId,
            dueEpochDay = todayEpochDay,
            isCompleted = false,
            completedAtEpochSecond = 0L,
            createdAtEpochDay = todayEpochDay,
        )
        dao.insertInstance(instance)
        return template.toMission(instance)
    }

    override suspend fun updateCustomMission(id: String, draft: MissionDraft): Mission? {
        val template = dao.getTemplate(id) ?: return null
        if (!template.isCustom) return null
        dao.upsertTemplate(
            template.copy(
                title = draft.title.trim(),
                description = draft.description.trim(),
                category = draft.category.name,
                difficulty = draft.difficulty.name,
                xpReward = draft.difficulty.xpReward,
                coinReward = draft.difficulty.coinReward,
                isRecurring = draft.isRecurring,
                durationMinutes = draft.durationMinutes ?: 0,
                reminderEnabled = draft.reminderEnabled,
                reminderHour = if (draft.reminderEnabled) draft.reminderHour else SeedData.REMINDER_OFF,
                reminderMinute = draft.reminderMinute,
            ),
        )
        val today = clock.todayEpochDay()
        return getMission(instanceId(id, today))
    }

    override suspend fun deleteCustomMission(id: String): Boolean {
        val template = dao.getTemplate(id) ?: return false
        if (!template.isCustom) return false
        dao.deleteInstancesForTemplate(id)
        dao.deleteTemplate(id)
        return true
    }

    override suspend fun pendingCountToday(todayEpochDay: Int): Int {
        val total = dao.countInstancesOnDay(todayEpochDay)
        val done = dao.countCompletedOnDay(todayEpochDay)
        return (total - done).coerceAtLeast(0)
    }

    override suspend fun countForDay(epochDay: Int): Pair<Int, Int> {
        val total = dao.countInstancesOnDay(epochDay)
        val done = dao.countCompletedOnDay(epochDay)
        return (total - done).coerceAtLeast(0) to total
    }

    override suspend fun pruneOldInstances(beforeEpochDay: Int) {
        dao.pruneInstancesBefore(beforeEpochDay)
    }

    companion object {
        private const val KEEP_DAYS = 60
        private const val CUSTOM_ORDER = 900
        private val starterIds = setOf(
            "mision_agua",
            "mision_estudiar",
            "mision_caminar",
            "mision_leer",
            "mision_ordenar",
        )

        fun instanceId(templateId: String, epochDay: Int): String = "$templateId:$epochDay"
    }
}
