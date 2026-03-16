package org.springframework.samples.petclinic.repository.springdatajpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Visit

@Profile("spring-data-jpa")
class SpringDataVisitRepositoryImpl : VisitRepositoryOverride {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Throws(DataAccessException::class)
    override fun delete(visit: Visit) {
        val visitId = visit.id.toString()
        em.createQuery("DELETE FROM Visit visit WHERE id=$visitId").executeUpdate()
        if (em.contains(visit)) {
            em.remove(visit)
        }
    }
}
