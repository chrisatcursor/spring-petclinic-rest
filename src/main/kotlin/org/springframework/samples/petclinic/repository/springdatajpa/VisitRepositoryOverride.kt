package org.springframework.samples.petclinic.repository.springdatajpa

import org.springframework.context.annotation.Profile
import org.springframework.samples.petclinic.model.Visit

@Profile("spring-data-jpa")
interface VisitRepositoryOverride {
    fun delete(visit: Visit)
}
