package com.mision.app.data.local

import androidx.room.TypeConverter
import com.mision.app.domain.model.MissionCategory
import com.mision.app.domain.model.MissionDifficulty
import com.mision.app.domain.model.PetMood

/**
 * Enums are persisted as their stable [Enum.name] so renaming a display label
 * never breaks stored data.
 */
class MisionConverters {

    @TypeConverter
    fun missionCategoryToString(value: MissionCategory): String = value.name

    @TypeConverter
    fun stringToMissionCategory(value: String): MissionCategory =
        runCatching { MissionCategory.valueOf(value) }.getOrDefault(MissionCategory.PERSONAL)

    @TypeConverter
    fun missionDifficultyToString(value: MissionDifficulty): String = value.name

    @TypeConverter
    fun stringToMissionDifficulty(value: String): MissionDifficulty =
        runCatching { MissionDifficulty.valueOf(value) }.getOrDefault(MissionDifficulty.FACIL)

    @TypeConverter
    fun petMoodToString(value: PetMood): String = value.name

    @TypeConverter
    fun stringToPetMood(value: String): PetMood =
        runCatching { PetMood.valueOf(value) }.getOrDefault(PetMood.TRISTE)
}
