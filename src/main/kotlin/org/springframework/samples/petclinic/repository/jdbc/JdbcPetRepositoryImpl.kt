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

import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.samples.petclinic.repository.PetRepository
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.stereotype.Repository
import java.util.ArrayList
import java.util.Collection
import java.util.List
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcPetRepositoryImpl(
    dataSource: DataSource,
    private val ownerRepository: OwnerRepository
) : PetRepository {

    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource)

    private val insertPet: SimpleJdbcInsert = SimpleJdbcInsert(dataSource)
        .withTableName("pets")
        .usingGeneratedKeyColumns("id")

    override fun findPetTypes(): List<PetType> {
        val params: MutableMap<String, Any> = HashMap()
        return ArrayList(
            namedParameterJdbcTemplate.query(
                "SELECT id, name FROM types ORDER BY name",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )
        ) as List<PetType>
    }

    override fun findById(id: Int): Pet {
        val ownerId = try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            namedParameterJdbcTemplate.queryForObject(
                "SELECT owner_id FROM pets WHERE id=:id",
                params,
                Int::class.java
            )
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Pet::class.java, id)
        }
        val owner = ownerRepository.findById(ownerId!!)
        return EntityUtils.getById(owner.getPets(), Pet::class.java, id)
    }

    override fun save(pet: Pet) {
        if (pet.isNew()) {
            val newKey = insertPet.executeAndReturnKey(createPetParameterSource(pet))
            pet.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE pets SET name=:name, birth_date=:birth_date, type_id=:type_id, " +
                    "owner_id=:owner_id WHERE id=:id",
                createPetParameterSource(pet)
            )
        }
    }

    private fun createPetParameterSource(pet: Pet): MapSqlParameterSource =
        MapSqlParameterSource()
            .addValue("id", pet.id)
            .addValue("name", pet.name)
            .addValue("birth_date", pet.birthDate)
            .addValue("type_id", pet.type!!.id)
            .addValue("owner_id", pet.owner!!.id)

    override fun findAll(): Collection<Pet> {
        val params: MutableMap<String, Any> = HashMap()
        val pets: MutableCollection<Pet> = ArrayList()
        val jdbcPets = namedParameterJdbcTemplate.query(
            "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id FROM pets",
            params,
            JdbcPetRowMapper()
        )
        val petTypes = namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name",
            HashMap<String, Any>(),
            BeanPropertyRowMapper.newInstance(PetType::class.java)
        )
        val owners = namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners ORDER BY last_name",
            HashMap<String, Any>(),
            BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        for (jdbcPet in jdbcPets) {
            jdbcPet.type = EntityUtils.getById(ArrayList(petTypes), PetType::class.java, jdbcPet.typeId)
            jdbcPet.owner = EntityUtils.getById(ArrayList(owners), Owner::class.java, jdbcPet.ownerId)
            pets.add(jdbcPet)
        }
        return ArrayList(pets) as Collection<Pet>
    }

    override fun delete(pet: Pet) {
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
}
