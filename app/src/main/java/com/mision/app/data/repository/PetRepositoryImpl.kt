package com.mision.app.data.repository

import com.mision.app.core.time.ClockProvider
import com.mision.app.data.local.PetEntity
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
        dao.observePet().map { (it ?: defaultPet()).toDomain() }

    override suspend fun getPet(): Pet = (dao.getPet() ?: defaultPet()).toDomain()

    override suspend fun savePet(pet: Pet) {
        dao.upsertPet(pet.toEntity())
    }

    override suspend fun resetPet(name: String) {
        dao.upsertPet(Pet.default(name, clock.todayEpochDay()).toEntity())
    }

    private fun defaultPet(): PetEntity =
        Pet.default(name = "Nube", epochDay = clock.todayEpochDay()).toEntity()
}
