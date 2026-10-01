package com.mision.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    // ---- Templates -------------------------------------------------------

    @Query("SELECT * FROM mission_templates WHERE isActive = 1 ORDER BY sortOrder ASC, title ASC")
    fun observeActiveTemplates(): Flow<List<MissionTemplateEntity>>

    @Query("SELECT * FROM mission_templates ORDER BY sortOrder ASC, title ASC")
    suspend fun getTemplates(): List<MissionTemplateEntity>

    @Query("SELECT * FROM mission_templates WHERE id = :id")
    suspend fun getTemplate(id: String): MissionTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplate(template: MissionTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplates(templates: List<MissionTemplateEntity>)

    @Query("DELETE FROM mission_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)

    // ---- Instances -------------------------------------------------------

    @Query("SELECT * FROM mission_instances WHERE dueEpochDay = :epochDay")
    fun observeInstancesForDay(epochDay: Int): Flow<List<MissionInstanceEntity>>

    @Query("SELECT * FROM mission_instances WHERE dueEpochDay = :epochDay")
    suspend fun getInstancesForDay(epochDay: Int): List<MissionInstanceEntity>

    @Query("SELECT * FROM mission_instances WHERE id = :id")
    suspend fun getInstance(id: String): MissionInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInstance(instance: MissionInstanceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInstances(instances: List<MissionInstanceEntity>)

    @Update
    suspend fun updateInstance(instance: MissionInstanceEntity)

    @Query("DELETE FROM mission_instances WHERE id = :id")
    suspend fun deleteInstance(id: String)

    @Query("DELETE FROM mission_instances WHERE templateId = :templateId")
    suspend fun deleteInstancesForTemplate(templateId: String)

    @Query("DELETE FROM mission_instances WHERE dueEpochDay < :epochDay")
    suspend fun pruneInstancesBefore(epochDay: Int)

    @Query(
        """
        SELECT COUNT(*) FROM mission_instances
        WHERE dueEpochDay = :epochDay AND isCompleted = 1
        """,
    )
    suspend fun countCompletedOnDay(epochDay: Int): Int

    @Query("SELECT COUNT(*) FROM mission_instances WHERE dueEpochDay = :epochDay")
    suspend fun countInstancesOnDay(epochDay: Int): Int
}
