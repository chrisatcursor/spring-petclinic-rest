package org.springframework.samples.petclinic.repository

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Vet

interface VetRepository {
    @Throws(DataAccessException::class)
    fun findAll(): Collection<Vet>

    @Throws(DataAccessException::class)
    fun findById(id: Int): Vet

    @Throws(DataAccessException::class)
    fun save(vet: Vet)

    @Throws(DataAccessException::class)
    fun delete(vet: Vet)
}
