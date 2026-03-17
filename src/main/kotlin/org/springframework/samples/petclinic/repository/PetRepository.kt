package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType

interface PetRepository {
    @Throws(DataAccessException::class)
    fun findPetTypes(): List<PetType>

    @Throws(DataAccessException::class)
    fun findById(id: Int): Pet

    @Throws(DataAccessException::class)
    fun save(pet: Pet)

    @Throws(DataAccessException::class)
    fun findAll(): Collection<Pet>

    @Throws(DataAccessException::class)
    fun delete(pet: Pet)
}
