package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.repository.VetRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaVetRepositoryImpl : VetRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Vet = em.find(Vet::class.java, id)

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Vet> =
        em.createQuery("SELECT vet FROM Vet vet").resultList as Collection<Vet>

    @Throws(DataAccessException::class)
    override fun save(vet: Vet) {
        if (vet.id == null) {
            em.persist(vet)
        } else {
            em.merge(vet)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(vet: Vet) {
        em.remove(if (em.contains(vet)) vet else em.merge(vet))
    }
}
