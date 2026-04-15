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
package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.PetTypeRepository
import org.springframework.stereotype.Repository
import java.util.ArrayList
import java.util.Collection
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcPetTypeRepositoryImpl(dataSource: DataSource) : PetTypeRepository {

    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource)

    private val insertPetType: SimpleJdbcInsert = SimpleJdbcInsert(dataSource)
        .withTableName("types")
        .usingGeneratedKeyColumns("id")

    override fun findById(id: Int): PetType {
        return try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(PetType::class.java, id)
        }
    }

    override fun findByName(name: String): PetType {
        return try {
            val params: MutableMap<String, Any> = HashMap()
            params["name"] = name
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE name= :name",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(PetType::class.java, name)
        }
    }

    override fun findAll(): Collection<PetType> {
        val params: MutableMap<String, Any> = HashMap()
        return ArrayList(
            namedParameterJdbcTemplate.query(
                "SELECT id, name FROM types",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )
        ) as Collection<PetType>
    }

    override fun save(petType: PetType) {
        val parameterSource = BeanPropertySqlParameterSource(petType)
        if (petType.isNew()) {
            val newKey = insertPetType.executeAndReturnKey(parameterSource)
            petType.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE types SET name=:name WHERE id=:id",
                parameterSource
            )
        }
    }

    override fun delete(petType: PetType) {
        val pettypeParams: MutableMap<String, Any> = HashMap()
        pettypeParams["id"] = petType.id!!
        val pets = namedParameterJdbcTemplate.query(
            "SELECT pets.id, name, birth_date, type_id, owner_id FROM pets WHERE type_id=:id",
            pettypeParams,
            BeanPropertyRowMapper.newInstance(Pet::class.java)
        )
        for (pet in pets) {
            val petParams: MutableMap<String, Any> = HashMap()
            petParams["id"] = pet.id!!
            val visits = namedParameterJdbcTemplate.query(
                "SELECT id, pet_id, visit_date, description FROM visits WHERE pet_id = :id",
                petParams,
                BeanPropertyRowMapper.newInstance(Visit::class.java)
            )
            for (visit in visits) {
                val visitParams: MutableMap<String, Any> = HashMap()
                visitParams["id"] = visit.id!!
                namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visitParams)
            }
            namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", petParams)
        }
        namedParameterJdbcTemplate.update("DELETE FROM types WHERE id=:id", pettypeParams)
    }
}
