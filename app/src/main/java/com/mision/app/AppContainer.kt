package com.mision.app

import android.content.Context
import com.mision.app.core.time.ClockProvider
import com.mision.app.core.time.SystemClockProvider
import com.mision.app.data.local.MisionDatabase
import com.mision.app.data.local.SettingsDataStore
import com.mision.app.data.repository.GamificationRepositoryImpl
import com.mision.app.data.repository.MissionRepositoryImpl
import com.mision.app.data.repository.PetRepositoryImpl
import com.mision.app.data.repository.ProgressRepositoryImpl
import com.mision.app.data.repository.SettingsRepositoryImpl
import com.mision.app.data.repository.ShopRepositoryImpl
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.domain.repository.ShopRepository
import com.mision.app.domain.usecase.UseCases
import com.mision.app.notifications.MisionNotifier
import com.mision.app.notifications.MissionReminderScheduler
import com.mision.app.notifications.ReminderScheduler

/**
 * Manual dependency injection container (composition root).
 *
 * A lightweight container is used instead of a full DI framework to keep the
 * build fast and the graph explicit. Everything is created once, lazily and
 * shared; ViewModels only depend on the domain interfaces exposed here.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val clock: ClockProvider = SystemClockProvider()

    val database: MisionDatabase by lazy { MisionDatabase.getInstance(appContext) }

    val dataStore: SettingsDataStore by lazy { SettingsDataStore(appContext) }

    val notifier: MisionNotifier by lazy { MisionNotifier(appContext) }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(appContext, clock) }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(dataStore, reminderScheduler)
    }

    val missionReminderScheduler: MissionReminderScheduler by lazy {
        MissionReminderScheduler(appContext, reminderScheduler)
    }

    val missionRepository: MissionRepository by lazy {
        MissionRepositoryImpl(
            dao = database.missionDao(),
            clock = clock,
            onRemindersChanged = { missionReminderScheduler.syncAll(missionRepository) },
        )
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(database.progressDao(), clock)
    }

    val petRepository: PetRepository by lazy {
        PetRepositoryImpl(database.progressDao(), clock)
    }

    val gamificationRepository: GamificationRepository by lazy {
        GamificationRepositoryImpl(database.progressDao())
    }

    val shopRepository: ShopRepository by lazy {
        ShopRepositoryImpl(database.progressDao(), clock)
    }

    /** Domain layer entry point shared by every ViewModel. */
    val useCases: UseCases by lazy {
        UseCases(
            missionRepository = missionRepository,
            progressRepository = progressRepository,
            gamificationRepository = gamificationRepository,
            shopRepository = shopRepository,
            petRepository = petRepository,
            settingsRepository = settingsRepository,
            clock = clock,
        )
    }
}
