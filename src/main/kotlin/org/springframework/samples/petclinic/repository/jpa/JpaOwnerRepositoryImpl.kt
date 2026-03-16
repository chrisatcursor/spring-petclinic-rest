package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Query
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaOwnerRepositoryImpl : OwnerRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Suppress("UNCHECKED_CAST")
    override fun findByLastName(lastName: String): Collection<Owner> {
        val query: Query =
            em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName")
        query.setParameter("lastName", "$lastName%")
        return query.resultList as Collection<Owner>
    }

    override fun findById(id: Int): Owner {
        val query: Query = em.createQuery("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
        query.setParameter("id", id)
        return query.singleResult as Owner
    }

    override fun save(owner: Owner) {
        if (owner.id == null) {
            em.persist(owner)
        } else {
            em.merge(owner)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Owner> {
        val query: Query = em.createQuery("SELECT owner FROM Owner owner")
        return query.resultList as Collection<Owner>
    }

    @Throws(DataAccessException::class)
    override fun delete(owner: Owner) {
        em.remove(if (em.contains(owner)) owner else em.merge(owner))
    }
}
