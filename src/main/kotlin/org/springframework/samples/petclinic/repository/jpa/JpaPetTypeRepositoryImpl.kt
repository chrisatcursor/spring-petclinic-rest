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
package org.springframework.samples.petclinic.repository.jpa

import java.util.Collection
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.repository.PetTypeRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaPetTypeRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : PetTypeRepository {

    override fun findById(id: Int): PetType {
        return em.find(PetType::class.java, id)
    }

    @Throws(DataAccessException::class)
    override fun findByName(name: String): PetType {
        return em.createQuery("SELECT p FROM PetType p WHERE p.name = :name", PetType::class.java)
            .setParameter("name", name)
            .singleResult
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<PetType> {
        return em.createQuery("SELECT ptype FROM PetType ptype").resultList as Collection<PetType>
    }

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

        val pets = em.createQuery("SELECT pet FROM Pet pet WHERE type.id=$petTypeId")
            .resultList as List<Pet>
        for (pet in pets) {
            val visits = pet.getVisits()
            for (visit in visits) {
                em.createQuery("DELETE FROM Visit visit WHERE id=${visit.id}").executeUpdate()
            }
            em.createQuery("DELETE FROM Pet pet WHERE id=${pet.id}").executeUpdate()
        }
        em.createQuery("DELETE FROM PetType pettype WHERE id=$petTypeId").executeUpdate()
    }
}
