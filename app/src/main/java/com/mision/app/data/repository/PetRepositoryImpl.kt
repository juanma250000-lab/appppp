package com.mision.app.data.repository

import com.mision.app.core.time.ClockProvider
import com.mision.app.data.local.ProgressDao
import com.mision.app.data.toDomain
import com.mision.app.data.toEntity
import com.mision.app.domain.model.Pet
import com.mision.app.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetRepositoryImpl(
    private val dao: ProgressDao,
    private val clock: ClockProvider,
) : PetRepository {

    override fun observePet(): Flow<Pet> =
        dao.observePet().map { it?.toDomain() ?: defaultPet() }

    override suspend fun getPet(): Pet = dao.getPet()?.toDomain() ?: defaultPet()

    override suspend fun savePet(pet: Pet) {
        val createdAt = dao.getPet()?.createdAtEpochDay ?: clock.todayEpochDay()
        dao.upsertPet(pet.toEntity(createdAtEpochDay = createdAt))
    }

    override suspend fun resetPet(name: String) {
        val today = clock.todayEpochDay()
        dao.upsertPet(Pet.default(name, today).toEntity(createdAtEpochDay = today))
    }

    private fun defaultPet(): Pet = Pet.default(epochDay = clock.todayEpochDay())
}
