package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.PetType

interface PetTypeRepository {
    @Throws(DataAccessException::class)
    fun findById(id: Int): PetType

    @Throws(DataAccessException::class)
    fun findByName(name: String): PetType

    @Throws(DataAccessException::class)
    fun findAll(): Collection<PetType>

    @Throws(DataAccessException::class)
    fun save(petType: PetType)

    @Throws(DataAccessException::class)
    fun delete(petType: PetType)
}
