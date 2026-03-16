package org.springframework.samples.petclinic.repository.springdatajpa

import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.repository.OwnerRepository

@Profile("spring-data-jpa")
interface SpringDataOwnerRepository : OwnerRepository, Repository<Owner, Int> {
    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    override fun findByLastName(@Param("lastName") lastName: String): Collection<Owner>

    @Query("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
    override fun findById(@Param("id") id: Int): Owner
}
