package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Specialty

interface SpecialtyRepository {
    @Throws(DataAccessException::class)
    fun findById(id: Int): Specialty

    fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty>

    @Throws(DataAccessException::class)
    fun findAll(): Collection<Specialty>

    @Throws(DataAccessException::class)
    fun save(specialty: Specialty)

    @Throws(DataAccessException::class)
    fun delete(specialty: Specialty)
}
