package com.mision.app.testing

import com.mision.app.core.gamification.ShopCatalog
import com.mision.app.core.time.ClockProvider
import com.mision.app.domain.model.DailyLog
import com.mision.app.domain.model.EquippedCosmetics
import com.mision.app.domain.model.Mission
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import com.mision.app.domain.model.Pet
import com.mision.app.domain.model.PurchaseResult
import com.mision.app.domain.model.ShopItem
import com.mision.app.domain.model.StreakState
import com.mision.app.domain.model.UserProfile
import com.mision.app.domain.repository.GamificationRepository
import com.mision.app.domain.repository.MissionDraft
import com.mision.app.domain.repository.MissionRepository
import com.mision.app.domain.repository.PetRepository
import com.mision.app.domain.repository.ProgressRepository
import com.mision.app.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Deterministic clock so every test can travel in time (midnight rollovers,
 * missed days...) without touching the wall clock.
 */
class FakeClock(epochDay: Int = DEFAULT_DAY) : ClockProvider {

    var epochDay: Int = epochDay
        private set

    var epochSecond: Long = 1_700_000_000L

    fun advanceDays(days: Int) {
        epochDay += days
    }

    override fun today(): LocalDate = LocalDate.ofEpochDay(epochDay.toLong())

    override fun now(): LocalDateTime = today().atTime(12, 0)

    override fun nowEpochSecond(): Long = epochSecond

    override fun zone(): ZoneId = ZoneId.of("UTC")

    companion object {
        /** 2026-09-15, an arbitrary stable anchor for every test. */
        const val DEFAULT_DAY = 20_000
    }
}

/** Builds a mission of a given day with sensible defaults. */
fun testMission(
    dueEpochDay: Int,
    id: String = "tpl:$dueEpochDay",
    templateId: String = "tpl",
    title: String = "Misión de prueba",
    xpReward: Int = 25,
    coinReward: Int = 10,
    isCompleted: Boolean = false,
    isCustom: Boolean = false,
    sortOrder: Int = 0,
): Mission = Mission(
    id = id,
    templateId = templateId,
    title = title,
    description = "",
    category = MissionCategory.SALUD,
    difficulty = MissionDifficulty.MEDIA,
    xpReward = xpReward,
    coinReward = coinReward,
    isCompleted = isCompleted,
    completedAtEpochSecond = if (isCompleted) 1_700_000_000L else 0L,
    dueEpochDay = dueEpochDay,
    createdAtEpochDay = dueEpochDay,
    isRecurring = true,
    isCustom = isCustom,
    reminderEnabled = false,
    reminderHour = 9,
    reminderMinute = 0,
    durationMinutes = null,
    sortOrder = sortOrder,
)

class FakeMissionRepository(initial: List<Mission> = emptyList()) : MissionRepository {

    private val missions = initial.toMutableList()
    private var nextTemplate = 0

    fun add(mission: Mission) {
        missions += mission
    }

    fun all(): List<Mission> = missions.toList()

    override fun observeMissionsForDay(epochDay: Int): Flow<List<Mission>> = flowOf(
        missions.filter { it.dueEpochDay == epochDay }.sortedBy { it.sortOrder },
    )

    override suspend fun getMission(id: String): Mission? = missions.firstOrNull { it.id == id }

    override suspend fun ensureDailyMissions(todayEpochDay: Int) {
        // Tests seed missions explicitly; the real repository materialises templates.
    }

    override suspend fun setCompleted(id: String, completed: Boolean, atEpochSecond: Long): Mission? {
        val index = missions.indexOfFirst { it.id == id }
        if (index < 0) return null
        val updated = missions[index].copy(
            isCompleted = completed,
            completedAtEpochSecond = if (completed) atEpochSecond else 0L,
        )
        missions[index] = updated
        return updated
    }

    override suspend fun addCustomMission(draft: MissionDraft, todayEpochDay: Int): Mission {
        val templateId = "custom-${nextTemplate++}"
        val mission = testMission(
            dueEpochDay = todayEpochDay,
            id = "$templateId:$todayEpochDay",
            templateId = templateId,
            title = draft.title,
            xpReward = draft.difficulty.xpReward,
            coinReward = draft.difficulty.coinReward,
            isCustom = true,
            sortOrder = missions.size,
        ).copy(
            description = draft.description,
            category = draft.category,
            difficulty = draft.difficulty,
            durationMinutes = draft.durationMinutes,
            reminderEnabled = draft.reminderEnabled,
            reminderHour = draft.reminderHour,
            reminderMinute = draft.reminderMinute,
            isRecurring = draft.isRecurring,
        )
        missions += mission
        return mission
    }

    override suspend fun updateCustomMission(id: String, draft: MissionDraft): Mission? {
        val index = missions.indexOfFirst { it.templateId == id && it.isCustom }
        if (index < 0) return null
        for (i in missions.indices) {
            if (missions[i].templateId == id) {
                missions[i] = missions[i].copy(
                    title = draft.title,
                    description = draft.description,
                    category = draft.category,
                    difficulty = draft.difficulty,
                )
            }
        }
        return missions[index]
    }

    override suspend fun deleteCustomMission(id: String): Boolean {
        val any = missions.any { it.templateId == id && it.isCustom }
        missions.removeAll { it.templateId == id && it.isCustom }
        return any
    }

    override suspend fun pendingCountToday(todayEpochDay: Int): Int =
        missions.count { it.dueEpochDay == todayEpochDay && !it.isCompleted }

    override suspend fun countForDay(epochDay: Int): Pair<Int, Int> {
        val ofDay = missions.filter { it.dueEpochDay == epochDay }
        return ofDay.count { !it.isCompleted } to ofDay.size
    }

    override suspend fun pruneOldInstances(beforeEpochDay: Int) {
        missions.removeAll { it.dueEpochDay < beforeEpochDay }
    }
}

class FakeProgressRepository(
    initialProfile: UserProfile = UserProfile.empty(name = "Ana", createdAtEpochDay = FakeClock.DEFAULT_DAY),
) : ProgressRepository {

    private val profileFlow = MutableStateFlow(initialProfile)
    private val logsFlow = MutableStateFlow<Map<Int, DailyLog>>(emptyMap())

    override fun observeProfile(): Flow<UserProfile> = profileFlow

    override suspend fun getProfile(): UserProfile = profileFlow.value

    override suspend fun saveProfile(profile: UserProfile) {
        profileFlow.value = profile
    }

    override fun observeDailyLogs(): Flow<List<DailyLog>> =
        logsFlow.map { it.values.sortedBy(DailyLog::epochDay) }

    override suspend fun getDailyLog(epochDay: Int): DailyLog? = logsFlow.value[epochDay]

    override suspend fun saveDailyLog(log: DailyLog) {
        logsFlow.value = logsFlow.value + (log.epochDay to log)
    }

    override suspend fun totalCoinsEarned(): Int = logsFlow.value.values.sumOf { it.coinsEarned }

    /** Mirrors the real repository: a perfect day has every mission done. */
    fun perfectDaysCount(): Int =
        logsFlow.value.values.count { it.totalMissions > 0 && it.completedCount >= it.totalMissions }

    override suspend fun resetProgress() {
        profileFlow.value = profileFlow.value.copy(
            totalXp = 0,
            coins = 0,
            totalMissionsCompleted = 0,
        )
        logsFlow.value = emptyMap()
    }
}

class FakeGamificationRepository(
    private val progress: FakeProgressRepository? = null,
) : GamificationRepository {

    private val streakFlow = MutableStateFlow(StreakState.empty())
    private val unlockedFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun observeStreak(): Flow<StreakState> = streakFlow

    override suspend fun getStreak(): StreakState = streakFlow.value

    override suspend fun saveStreak(state: StreakState) {
        streakFlow.value = state
    }

    override fun observeUnlockedAchievementIds(): Flow<Set<String>> = unlockedFlow

    override suspend fun getUnlockedAchievementIds(): Set<String> = unlockedFlow.value

    override suspend fun unlockAchievements(ids: List<String>, unlockedAtEpochSecond: Long) {
        if (ids.isEmpty()) return
        unlockedFlow.value = unlockedFlow.value + ids
    }

    override suspend fun countPerfectDays(): Int = progress?.perfectDaysCount() ?: 0
}

class FakePetRepository(initial: Pet = Pet.default("Nube", FakeClock.DEFAULT_DAY)) : PetRepository {

    private val petFlow = MutableStateFlow(initial)

    override fun observePet(): Flow<Pet> = petFlow

    override suspend fun getPet(): Pet = petFlow.value

    override suspend fun savePet(pet: Pet) {
        petFlow.value = pet
    }

    override suspend fun resetPet(name: String) {
        petFlow.value = Pet.default(name, FakeClock.DEFAULT_DAY)
    }
}

class FakeShopRepository : ShopRepository {

    private val purchasesFlow = MutableStateFlow<Set<String>>(emptySet())
    private val equippedFlow = MutableStateFlow(EquippedCosmetics())

    fun seedPurchases(ids: Set<String>) {
        purchasesFlow.value = ids
    }

    override fun observeCatalog(): Flow<List<ShopItem>> = flowOf(ShopCatalog.items)

    override fun observePurchases(): Flow<Set<String>> = purchasesFlow

    override suspend fun getPurchases(): Set<String> = purchasesFlow.value

    override suspend fun recordPurchase(itemId: String, atEpochSecond: Long) {
        purchasesFlow.value = purchasesFlow.value + itemId
    }

    override fun observeEquipped(): Flow<EquippedCosmetics> = equippedFlow

    override suspend fun equip(itemId: String) {
        val item = ShopCatalog.byId(itemId) ?: return
        val current = equippedFlow.value.idFor(item.category)
        equippedFlow.value = equippedFlow.value.with(
            item.category,
            if (current == itemId) null else itemId,
        )
    }

    /** Same decision table as the production repository. */
    override suspend fun purchaseResult(item: ShopItem, currentCoins: Int): PurchaseResult = when {
        item.id in purchasesFlow.value -> PurchaseResult.AlreadyOwned
        currentCoins < item.cost -> PurchaseResult.NotEnoughCoins(item.cost - currentCoins)
        else -> PurchaseResult.Success(item, currentCoins - item.cost)
    }
}
