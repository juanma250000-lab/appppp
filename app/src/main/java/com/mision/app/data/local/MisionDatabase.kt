package com.mision.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Single source of truth for local structured data.
 *
 * The app is fully offline: profile, pet, missions, streaks, achievements,
 * purchases and daily statistics all live here.
 */
@Database(
    entities = [
        MissionTemplateEntity::class,
        MissionInstanceEntity::class,
        ProfileEntity::class,
        PetEntity::class,
        StreakEntity::class,
        AchievementEntity::class,
        PurchaseEntity::class,
        DailyLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MisionDatabase : RoomDatabase() {

    abstract fun missionDao(): MissionDao
    abstract fun progressDao(): ProgressDao

    companion object {
        const val DATABASE_NAME = "mision.db"

        @Volatile
        private var instance: MisionDatabase? = null

        fun getInstance(context: Context): MisionDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): MisionDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                MisionDatabase::class.java,
                DATABASE_NAME,
            ).build()
    }
}
