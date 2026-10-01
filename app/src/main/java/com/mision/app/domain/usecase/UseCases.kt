package com.mision.app.domain.usecase

import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.repository.ShopRepository
import com.mision.app.domain.repository.TransactionRunner

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
    transaction: TransactionRunner,
    clock: ClockProvider,
) {
    // ---- Missions --------------------------------------------------------
    val getDailyMissions = GetDailyMissionsUseCase(missionRepository, settingsRepository)
    val ensureDailyMissions = EnsureDailyMissionsUseCase(missionRepository, clock)
    val completeMission = CompleteMissionUseCase(
        missionRepository = missionRepository,
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        shopRepository = shopRepository,
        petRepository = petRepository,
        transaction = transaction,
        clock = clock,
    )
    val uncompleteMission = UncompleteMissionUseCase(
        missionRepository = missionRepository,
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        petRepository = petRepository,
        transaction = transaction,
        clock = clock,
    )
    val createCustomMission = CreateCustomMissionUseCase(missionRepository, clock)
    val updateCustomMission = UpdateCustomMissionUseCase(missionRepository)
    val deleteCustomMission = DeleteCustomMissionUseCase(missionRepository)

    // ---- Progression -----------------------------------------------------
    val updateProfileName = UpdateProfileNameUseCase(progressRepository)
    val completeOnboarding = CompleteOnboardingUseCase(
        progressRepository = progressRepository,
        petRepository = petRepository,
        settingsRepository = settingsRepository,
        transaction = transaction,
    )
    val resetProgress = ResetProgressUseCase(
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        petRepository = petRepository,
        missionRepository = missionRepository,
        transaction = transaction,
        clock = clock,
    )

    // ---- Streak ----------------------------------------------------------
    val calculateStreak = CalculateStreakUseCase(gamificationRepository, clock)

    // ---- Pet -------------------------------------------------------------
    val updatePetMood = UpdatePetMoodUseCase(petRepository, missionRepository, clock)
    val interactWithPet = InteractWithPetUseCase(petRepository, missionRepository, transaction, clock)

    // ---- Shop ------------------------------------------------------------
    val purchaseReward = PurchaseRewardUseCase(
        shopRepository = shopRepository,
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        transaction = transaction,
        clock = clock,
    )
    val equipReward = EquipRewardUseCase(shopRepository, transaction)

    // ---- Profile ---------------------------------------------------------
    val getAchievements = GetAchievementsUseCase(
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        shopRepository = shopRepository,
    )
    val getProfileStats = GetProfileStatsUseCase(
        progressRepository = progressRepository,
        gamificationRepository = gamificationRepository,
        missionRepository = missionRepository,
        clock = clock,
    )
}
