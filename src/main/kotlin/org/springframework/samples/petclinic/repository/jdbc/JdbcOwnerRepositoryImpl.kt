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
package org.springframework.samples.petclinic.repository.jdbc

import jakarta.transaction.Transactional
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Profile
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.stereotype.Repository
import java.util.ArrayList
import java.util.Collection
import javax.sql.DataSource

/**
 * A simple JDBC-based implementation of the [OwnerRepository] interface.
 */
@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcOwnerRepositoryImpl(dataSource: DataSource) : OwnerRepository {

    private val insertOwner: SimpleJdbcInsert = SimpleJdbcInsert(dataSource)
        .withTableName("owners")
        .usingGeneratedKeyColumns("id")

    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource)

    override fun findByLastName(lastName: String): Collection<Owner> {
        val params: MutableMap<String, Any> = HashMap()
        params["lastName"] = "$lastName%"
        val owners = namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like :lastName",
            params,
            BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        loadOwnersPetsAndVisits(owners)
        return ArrayList(owners) as Collection<Owner>
    }

    override fun findById(id: Int): Owner {
        val owner: Owner = try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(Owner::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Owner::class.java, id)
        }
        loadPetsAndVisits(owner)
        return owner
    }

    fun loadPetsAndVisits(owner: Owner) {
        val params: MutableMap<String, Any> = HashMap()
        params["id"] = owner.id!!
        val pets = namedParameterJdbcTemplate.query(
            "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id, visits.id as visit_id, visit_date, description, visits.pet_id as visits_pet_id FROM pets LEFT OUTER JOIN visits ON pets.id = visits.pet_id WHERE owner_id=:id ORDER BY pets.id",
            params,
            JdbcPetVisitExtractor()
        )
        val petTypes = namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name",
            HashMap<String, Any>(),
            BeanPropertyRowMapper.newInstance(PetType::class.java)
        )
        for (pet in pets) {
            pet.type = EntityUtils.getById(petTypes, PetType::class.java, pet.typeId)
            owner.addPet(pet)
        }
    }

    override fun save(owner: Owner) {
        val parameterSource = BeanPropertySqlParameterSource(owner)
        if (owner.isNew()) {
            val newKey = insertOwner.executeAndReturnKey(parameterSource)
            owner.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, " +
                    "city=:city, telephone=:telephone WHERE id=:id",
                parameterSource
            )
        }
    }

    fun getPetTypes(): Collection<PetType> =
        ArrayList(
            namedParameterJdbcTemplate.query(
                "SELECT id, name FROM types ORDER BY name",
                HashMap<String, Any>(),
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )
        ) as Collection<PetType>

    private fun loadOwnersPetsAndVisits(owners: List<Owner>) {
        for (owner in owners) {
            loadPetsAndVisits(owner)
        }
    }

    override fun findAll(): Collection<Owner> {
        val owners = namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners",
            HashMap<String, Any>(),
            BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        for (owner in owners) {
            loadPetsAndVisits(owner)
        }
        return ArrayList(owners) as Collection<Owner>
    }

    @Transactional
    override fun delete(owner: Owner) {
        val ownerParams: MutableMap<String, Any> = HashMap()
        ownerParams["id"] = owner.id!!
        val pets = owner.getPets()
        for (pet in pets) {
            val petParams: MutableMap<String, Any> = HashMap()
            petParams["id"] = pet.id!!
            val visits = pet.getVisits()
            for (visit in visits) {
                val visitParams: MutableMap<String, Any> = HashMap()
                visitParams["id"] = visit.id!!
                namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visitParams)
            }
            namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", petParams)
        }
        namedParameterJdbcTemplate.update("DELETE FROM owners WHERE id=:id", ownerParams)
    }
}
