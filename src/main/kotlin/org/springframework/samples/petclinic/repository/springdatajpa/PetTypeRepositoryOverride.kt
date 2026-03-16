package org.springframework.samples.petclinic.repository.springdatajpa

import org.springframework.context.annotation.Profile
import org.springframework.samples.petclinic.model.PetType

@Profile("spring-data-jpa")
interface PetTypeRepositoryOverride {
    fun delete(petType: PetType)
}
