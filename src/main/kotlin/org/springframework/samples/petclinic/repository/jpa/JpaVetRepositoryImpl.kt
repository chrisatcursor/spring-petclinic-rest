/*
 * Copyright 2002-2017 the original author or authors.
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
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.repository.VetRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaVetRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : VetRepository {

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Vet {
        return em.find(Vet::class.java, id)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Vet> {
        return em.createQuery("SELECT vet FROM Vet vet").resultList as Collection<Vet>
    }

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
