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
import java.util.List
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaVisitRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : VisitRepository {

    override fun save(visit: Visit) {
        if (visit.id == null) {
            em.persist(visit)
        } else {
            em.merge(visit)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun findByPetId(petId: Integer?): List<Visit> {
        val query = em.createQuery("SELECT v FROM Visit v where v.pet.id= :id")
        query.setParameter("id", petId)
        return query.resultList as List<Visit>
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Visit {
        return em.find(Visit::class.java, id)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Visit> {
        return em.createQuery("SELECT v FROM Visit v").resultList as Collection<Visit>
    }

    @Throws(DataAccessException::class)
    override fun delete(visit: Visit) {
        em.remove(if (em.contains(visit)) visit else em.merge(visit))
    }
}
