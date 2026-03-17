package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.PetTypeRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaPetTypeRepositoryImpl : PetTypeRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun findById(id: Int): PetType = em.find(PetType::class.java, id)

    @Throws(DataAccessException::class)
    override fun findByName(name: String): PetType =
        em.createQuery("SELECT p FROM PetType p WHERE p.name = :name", PetType::class.java)
            .setParameter("name", name)
            .singleResult

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<PetType> =
        em.createQuery("SELECT ptype FROM PetType ptype").resultList as Collection<PetType>

    @Throws(DataAccessException::class)
    override fun save(petType: PetType) {
        if (petType.id == null) {
            em.persist(petType)
        } else {
            em.merge(petType)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun delete(petType: PetType) {
        em.remove(if (em.contains(petType)) petType else em.merge(petType))
        val petTypeId = petType.id

        val pets = em.createQuery("SELECT pet FROM Pet pet WHERE type.id=$petTypeId").resultList as List<Pet>
        for (pet in pets) {
            val visits: List<Visit> = pet.getVisits()
            for (visit in visits) {
                em.createQuery("DELETE FROM Visit visit WHERE id=${visit.id}").executeUpdate()
            }
            em.createQuery("DELETE FROM Pet pet WHERE id=${pet.id}").executeUpdate()
        }
        em.createQuery("DELETE FROM PetType pettype WHERE id=$petTypeId").executeUpdate()
    }
}
