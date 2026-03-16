package org.springframework.samples.petclinic.repository.springdatajpa

import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.repository.PetRepository

@Profile("spring-data-jpa")
interface SpringDataPetRepository : PetRepository, Repository<Pet, Int>, PetRepositoryOverride {
    @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
    @Throws(DataAccessException::class)
    override fun findPetTypes(): List<PetType>
}
