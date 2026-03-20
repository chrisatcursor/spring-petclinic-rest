/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        val petTypeId = checkNotNull(petType.id)

        @Suppress("UNCHECKED_CAST")
        val pets = em.createQuery("SELECT pet FROM Pet pet WHERE type.id=$petTypeId")
            .resultList as List<Pet>
        for (pet in pets) {
            val visits = pet.getVisits()
            for (visit in visits) {
                em.createQuery("DELETE FROM Visit visit WHERE id=${checkNotNull(visit.id)}").executeUpdate()
            }
            em.createQuery("DELETE FROM Pet pet WHERE id=${checkNotNull(pet.id)}").executeUpdate()
        }
        em.createQuery("DELETE FROM PetType pettype WHERE id=$petTypeId").executeUpdate()
    }
}
