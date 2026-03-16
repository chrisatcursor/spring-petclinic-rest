package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Query
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaVisitRepositoryImpl : VisitRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun save(visit: Visit) {
        if (visit.id == null) {
            em.persist(visit)
        } else {
            em.merge(visit)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun findByPetId(petId: Int?): List<Visit> {
        val query: Query = em.createQuery("SELECT v FROM Visit v where v.pet.id= :id")
        query.setParameter("id", petId)
        return query.resultList as List<Visit>
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Visit = em.find(Visit::class.java, id)

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Visit> =
        em.createQuery("SELECT v FROM Visit v").resultList as Collection<Visit>

    @Throws(DataAccessException::class)
    override fun delete(visit: Visit) {
        em.remove(if (em.contains(visit)) visit else em.merge(visit))
    }
}
