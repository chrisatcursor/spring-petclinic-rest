package org.springframework.samples.petclinic.repository.springdatajpa

import org.springframework.context.annotation.Profile
import org.springframework.data.repository.Repository
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.repository.PetTypeRepository

@Profile("spring-data-jpa")
interface SpringDataPetTypeRepository : PetTypeRepository, Repository<PetType, Int>, PetTypeRepositoryOverride
