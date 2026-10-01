package com.mision.app.domain.usecase

import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.repository.ShopRepository

/**
 * Manual dependency container for the domain layer.
 *
 * Every use case is created exactly once here so ViewModels can depend on a
 * single object instead of a dozen constructors, and the app keeps a single,
 * explicit object graph (no reflection, no annotation processing).
 */
class UseCases(
    missionRepository: MissionRepository,
    progressRepository: ProgressRepository,
    gamificationRepository: GamificationRepository,
    shopRepository: ShopRepository,
    petRepository: PetRepository,
    settingsRepository: SettingsRepository,
    clock: ClockProvider,
) {
    // ---- Missions --------------------------------------------------------
    val getDailyMissions = GetDailyMissionsUseCase(missionRepository)
    val ensureDailyMissions = EnsureDailyMissionsUseCase(missionRepository, clock)
    val completeMission = CompleteMissionUseCase(
        missionRepository = missionRepository,
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        shopRepository = shopRepository,
        petRepository = petRepository,
        clock = clock,
    )
    val uncompleteMission = UncompleteMissionUseCase(
        missionRepository = missionRepository,
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        petRepository = petRepository,
        clock = clock,
    )
    val createCustomMission = CreateCustomMissionUseCase(missionRepository, clock)
    val updateCustomMission = UpdateCustomMissionUseCase(missionRepository)
    val deleteCustomMission = DeleteCustomMissionUseCase(missionRepository)

    // ---- Progression -----------------------------------------------------
    val addExperience = AddExperienceUseCase(progressRepository)
    val levelUp = LevelUpUseCase(progressRepository)
    val updateProfileName = UpdateProfileNameUseCase(progressRepository)
    val setPreferredCategories = SetPreferredCategoriesUseCase(settingsRepository)
    val getDailyLog = GetDailyLogUseCase(progressRepository)
    val resetProgress = ResetProgressUseCase(
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        petRepository = petRepository,
        missionRepository = missionRepository,
        clock = clock,
    )

    // ---- Streak ----------------------------------------------------------
    val calculateStreak = CalculateStreakUseCase(gamificationRepository, clock)

    // ---- Pet -------------------------------------------------------------
    val updatePetMood = UpdatePetMoodUseCase(petRepository, missionRepository, clock)
    val interactWithPet = InteractWithPetUseCase(petRepository, missionRepository, clock)

    // ---- Shop ------------------------------------------------------------
    val purchaseReward = PurchaseRewardUseCase(
        shopRepository = shopRepository,
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        clock = clock,
    )
    val equipReward = EquipRewardUseCase(shopRepository)
    val getAchievements = GetAchievementsUseCase(
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        shopRepository = shopRepository,
    )

    // ---- Profile ---------------------------------------------------------
    val getProfileStats = GetProfileStatsUseCase(
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        missionRepository = missionRepository,
        clock = clock,
    )
}
