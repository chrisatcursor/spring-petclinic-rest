package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Visit

interface VisitRepository {
    @Throws(DataAccessException::class)
    fun save(visit: Visit)

    fun findByPetId(petId: Int?): List<Visit>

    @Throws(DataAccessException::class)
    fun findById(id: Int): Visit

    @Throws(DataAccessException::class)
    fun findAll(): Collection<Visit>

    @Throws(DataAccessException::class)
    fun delete(visit: Visit)
}
