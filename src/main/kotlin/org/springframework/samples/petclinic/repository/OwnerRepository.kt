package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Owner

interface OwnerRepository {
    @Throws(DataAccessException::class)
    fun findByLastName(lastName: String): Collection<Owner>

    @Throws(DataAccessException::class)
    fun findById(id: Int): Owner

    @Throws(DataAccessException::class)
    fun save(owner: Owner)

    @Throws(DataAccessException::class)
    fun findAll(): Collection<Owner>

    @Throws(DataAccessException::class)
    fun delete(owner: Owner)
}
