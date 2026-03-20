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
import java.util.List
import java.util.Set
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaSpecialtyRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : SpecialtyRepository {

    override fun findById(id: Int): Specialty {
        return em.find(Specialty::class.java, id)
    }

    @Suppress("UNCHECKED_CAST")
    override fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty> {
        return em.createQuery("SELECT s FROM Specialty s WHERE s.name IN :names", Specialty::class.java)
            .setParameter("names", names)
            .resultList as List<Specialty>
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Specialty> {
        return em.createQuery("SELECT s FROM Specialty s").resultList as Collection<Specialty>
    }

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
