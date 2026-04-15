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
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.stereotype.Repository
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Set
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcSpecialtyRepositoryImpl(dataSource: DataSource) : SpecialtyRepository {

    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource)

    private val insertSpecialty: SimpleJdbcInsert = SimpleJdbcInsert(dataSource)
        .withTableName("specialties")
        .usingGeneratedKeyColumns("id")

    override fun findById(id: Int): Specialty {
        return try {
            val params: MutableMap<String, Any> = HashMap()
            params["id"] = id
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM specialties WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(Specialty::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Specialty::class.java, id)
        }
    }

    override fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty> {
        return try {
            val sql = "SELECT id, name FROM specialties WHERE specialties.name IN (:names)"
            val params: MutableMap<String, Any> = HashMap()
            params["names"] = names
            ArrayList(
                namedParameterJdbcTemplate.query(
                    sql,
                    params,
                    BeanPropertyRowMapper.newInstance(Specialty::class.java)
                )
            ) as List<Specialty>
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Specialty::class.java, names)
        }
    }

    override fun findAll(): Collection<Specialty> {
        val params: MutableMap<String, Any> = HashMap()
        return ArrayList(
            namedParameterJdbcTemplate.query(
                "SELECT id, name FROM specialties",
                params,
                BeanPropertyRowMapper.newInstance(Specialty::class.java)
            )
        ) as Collection<Specialty>
    }

    override fun save(specialty: Specialty) {
        val parameterSource = BeanPropertySqlParameterSource(specialty)
        if (specialty.isNew()) {
            val newKey = insertSpecialty.executeAndReturnKey(parameterSource)
            specialty.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE specialties SET name=:name WHERE id=:id",
                parameterSource
            )
        }
    }

    override fun delete(specialty: Specialty) {
        val params: MutableMap<String, Any> = HashMap()
        params["id"] = specialty.id!!
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE specialty_id=:id", params)
        namedParameterJdbcTemplate.update("DELETE FROM specialties WHERE id=:id", params)
    }
}
