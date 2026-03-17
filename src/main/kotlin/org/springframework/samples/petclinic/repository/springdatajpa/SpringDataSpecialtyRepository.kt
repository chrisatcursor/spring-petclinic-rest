package org.springframework.samples.petclinic.repository.springdatajpa

import org.springframework.context.annotation.Profile
import org.springframework.data.repository.Repository
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.repository.SpecialtyRepository

@Profile("spring-data-jpa")
interface SpringDataSpecialtyRepository : SpecialtyRepository, Repository<Specialty, Int>, SpecialtyRepositoryOverride
