package org.springframework.samples.petclinic.repository.springdatajpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit

@Profile("spring-data-jpa")
class SpringDataPetTypeRepositoryImpl : PetTypeRepositoryOverride {
    @PersistenceContext
    private lateinit var em: EntityManager

    override fun delete(petType: PetType) {
        em.remove(if (em.contains(petType)) petType else em.merge(petType))
        val petTypeId = petType.id

        @Suppress("UNCHECKED_CAST")
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
