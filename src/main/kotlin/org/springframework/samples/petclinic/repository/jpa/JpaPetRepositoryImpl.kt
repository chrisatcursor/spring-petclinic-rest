package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.repository.PetRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaPetRepositoryImpl : PetRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Suppress("UNCHECKED_CAST")
    override fun findPetTypes(): List<PetType> =
        em.createQuery("SELECT ptype FROM PetType ptype ORDER BY ptype.name").resultList as List<PetType>

    override fun findById(id: Int): Pet = em.find(Pet::class.java, id)

    override fun save(pet: Pet) {
        if (pet.id == null) {
            em.persist(pet)
        } else {
            em.merge(pet)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Pet> =
        em.createQuery("SELECT pet FROM Pet pet").resultList as Collection<Pet>

    @Throws(DataAccessException::class)
    override fun delete(pet: Pet) {
        val petId = pet.id.toString()
        em.createQuery("DELETE FROM Visit visit WHERE pet.id=$petId").executeUpdate()
        em.createQuery("DELETE FROM Pet pet WHERE id=$petId").executeUpdate()
        if (em.contains(pet)) {
            em.remove(pet)
        }
    }
}
