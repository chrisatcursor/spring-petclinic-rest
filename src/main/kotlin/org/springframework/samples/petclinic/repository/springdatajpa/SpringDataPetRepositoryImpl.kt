package org.springframework.samples.petclinic.repository.springdatajpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.samples.petclinic.model.Pet

@Profile("spring-data-jpa")
class SpringDataPetRepositoryImpl : PetRepositoryOverride {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun delete(pet: Pet) {
        val petId = pet.id.toString()
        em.createQuery("DELETE FROM Visit visit WHERE pet.id=$petId").executeUpdate()
        em.createQuery("DELETE FROM Pet pet WHERE id=$petId").executeUpdate()
        if (em.contains(pet)) {
            em.remove(pet)
        }
    }
}
