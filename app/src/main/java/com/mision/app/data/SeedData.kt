package com.mision.app.data

import com.mision.app.data.local.MissionTemplateEntity
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty

/**
 * Spanish starter missions generated on first launch so the app is usable the
 * moment it is installed.
 */
object SeedData {

    /**
     * Value stored in the legacy per-mission reminder columns. Only the daily
     * reminder exists, so those columns are always written as "off".
     */
    private const val NO_REMINDER_HOUR = -1

    fun defaultTemplates(todayEpochDay: Int): List<MissionTemplateEntity> = listOf(
        template(
            id = "mision_agua",
            title = "Beber agua",
            description = "Completa tu objetivo de hidratación del día.",
            category = MissionCategory.SALUD,
            difficulty = MissionDifficulty.FACIL,
            sortOrder = 0,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_estudiar",
            title = "Estudiar 30 minutos",
            description = "Dedica al menos 30 minutos a una actividad académica.",
            category = MissionCategory.ESTUDIO,
            difficulty = MissionDifficulty.MEDIA,
            sortOrder = 1,
            durationMinutes = 30,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_caminar",
            title = "Caminar 20 minutos",
            description = "Realiza una caminata de 20 minutos.",
            category = MissionCategory.ACTIVIDAD_FISICA,
            difficulty = MissionDifficulty.MEDIA,
            sortOrder = 2,
            durationMinutes = 20,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_leer",
            title = "Leer 15 minutos",
            description = "Lee durante al menos 15 minutos.",
            category = MissionCategory.BIENESTAR,
            difficulty = MissionDifficulty.FACIL,
            sortOrder = 3,
            durationMinutes = 15,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_ordenar",
            title = "Ordenar tu espacio",
            description = "Dedica unos minutos a organizar tu espacio.",
            category = MissionCategory.PRODUCTIVIDAD,
            difficulty = MissionDifficulty.FACIL,
            sortOrder = 4,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_meditar",
            title = "Meditar 10 minutos",
            description = "Respira, relájate y observa tus pensamientos durante 10 minutos.",
            category = MissionCategory.BIENESTAR,
            difficulty = MissionDifficulty.FACIL,
            sortOrder = 5,
            durationMinutes = 10,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_tarea",
            title = "Completar una tarea académica",
            description = "Termina una tarea pendiente de tus asignaturas.",
            category = MissionCategory.ESTUDIO,
            difficulty = MissionDifficulty.DIFICIL,
            sortOrder = 6,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_ejercicio",
            title = "Hacer ejercicio",
            description = "Muévete durante 45 minutos: fuerza, cardio o lo que más te guste.",
            category = MissionCategory.ACTIVIDAD_FISICA,
            difficulty = MissionDifficulty.DIFICIL,
            sortOrder = 7,
            durationMinutes = 45,
            createdAt = todayEpochDay,
        ),
        template(
            id = "mision_dormir",
            title = "Dormir a tu hora",
            description = "Prepárate y acuéstate antes de las 23:00.",
            category = MissionCategory.SALUD,
            difficulty = MissionDifficulty.EPICA,
            sortOrder = 8,
            createdAt = todayEpochDay,
        ),
    )

    fun template(
        id: String,
        title: String,
        description: String,
        category: MissionCategory,
        difficulty: MissionDifficulty,
        sortOrder: Int,
        durationMinutes: Int = 0,
        isRecurring: Boolean = true,
        isCustom: Boolean = false,
        createdAt: Int,
    ): MissionTemplateEntity = MissionTemplateEntity(
        id = id,
        title = title,
        description = description,
        category = category.name,
        difficulty = difficulty.name,
        xpReward = difficulty.xpReward,
        coinReward = difficulty.coinReward,
        isRecurring = isRecurring,
        isCustom = isCustom,
        reminderEnabled = false,
        reminderHour = NO_REMINDER_HOUR,
        reminderMinute = 0,
        durationMinutes = durationMinutes,
        sortOrder = sortOrder,
        isActive = true,
        createdAtEpochDay = createdAt,
    )
}
