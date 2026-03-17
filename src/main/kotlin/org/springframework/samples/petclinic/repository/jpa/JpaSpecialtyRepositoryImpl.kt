package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaSpecialtyRepositoryImpl : SpecialtyRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun findById(id: Int): Specialty = em.find(Specialty::class.java, id)

    override fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty> {
        val jpql = "SELECT s FROM Specialty s WHERE s.name IN :names"
        return em.createQuery(jpql, Specialty::class.java)
            .setParameter("names", names)
            .resultList
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Specialty> =
        em.createQuery("SELECT s FROM Specialty s").resultList as Collection<Specialty>

    @Throws(DataAccessException::class)
    override fun save(specialty: Specialty) {
        if (specialty.id == null) {
            em.persist(specialty)
        } else {
            em.merge(specialty)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(specialty: Specialty) {
        em.remove(if (em.contains(specialty)) specialty else em.merge(specialty))
        val specId = specialty.id
        em.createNativeQuery("DELETE FROM vet_specialties WHERE specialty_id=$specId").executeUpdate()
        em.createQuery("DELETE FROM Specialty specialty WHERE id=$specId").executeUpdate()
    }
}
