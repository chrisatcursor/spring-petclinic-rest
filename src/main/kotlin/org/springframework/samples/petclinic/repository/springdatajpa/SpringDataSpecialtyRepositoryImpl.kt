package org.springframework.samples.petclinic.repository.springdatajpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.samples.petclinic.model.Specialty

@Profile("spring-data-jpa")
class SpringDataSpecialtyRepositoryImpl : SpecialtyRepositoryOverride {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun delete(specialty: Specialty) {
        em.remove(if (em.contains(specialty)) specialty else em.merge(specialty))
        val specId = specialty.id
        em.createNativeQuery("DELETE FROM vet_specialties WHERE specialty_id=$specId").executeUpdate()
        em.createQuery("DELETE FROM Specialty specialty WHERE id=$specId").executeUpdate()
    }
}
