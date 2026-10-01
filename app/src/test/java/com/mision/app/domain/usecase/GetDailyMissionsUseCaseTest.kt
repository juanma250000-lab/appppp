package com.mision.app.domain.usecase

import com.mision.app.domain.model.AppSettings
import com.mision.app.domain.model.MissionCategory
import com.mision.app.testing.FakeClock
import com.mision.app.testing.FakeMissionRepository
import com.mision.app.testing.FakeSettingsRepository
import com.mision.app.testing.testMission
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDailyMissionsUseCaseTest {

    private val today = FakeClock.DEFAULT_DAY

    private val missions = FakeMissionRepository(
        listOf(
            testMission(today, id = "salud", templateId = "salud", sortOrder = 0, category = MissionCategory.SALUD),
            testMission(today, id = "estudio", templateId = "estudio", sortOrder = 1, category = MissionCategory.ESTUDIO),
            testMission(
                today,
                id = "hecha",
                templateId = "hecha",
                sortOrder = 2,
                category = MissionCategory.ESTUDIO,
                isCompleted = true,
            ),
            testMission(today, id = "personal", templateId = "personal", sortOrder = 3, category = MissionCategory.PERSONAL),
        ),
    )

    private suspend fun orderedIds(favourites: Set<MissionCategory>): List<String> {
        val useCase = GetDailyMissionsUseCase(
            missionRepository = missions,
            settingsRepository = FakeSettingsRepository(AppSettings(preferredCategories = favourites)),
        )
        return useCase(today).first().map { it.id }
    }

    @Test
    fun `without favourites pending missions keep the repository order`() = runTest {
        assertEquals(listOf("salud", "estudio", "personal", "hecha"), orderedIds(emptySet()))
    }

    @Test
    fun `pending missions of favourite categories come first`() = runTest {
        // Completed missions stay at the end even when they are favourites.
        assertEquals(
            listOf("estudio", "personal", "salud", "hecha"),
            orderedIds(setOf(MissionCategory.ESTUDIO, MissionCategory.PERSONAL)),
        )
    }
}
