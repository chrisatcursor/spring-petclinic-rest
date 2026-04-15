/*
 * Copyright 2002-2018 the original author or authors.
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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.repository.VetRepository
import org.springframework.samples.petclinic.util.EntityUtils
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.ArrayList
import java.util.Collection
import javax.sql.DataSource

/**
 * A simple JDBC-based implementation of the [VetRepository] interface.
 */
@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcVetRepositoryImpl(
    dataSource: DataSource,
    private val jdbcTemplate: JdbcTemplate
) : VetRepository {

    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(dataSource)

    private val insertVet: SimpleJdbcInsert = SimpleJdbcInsert(dataSource)
        .withTableName("vets")
        .usingGeneratedKeyColumns("id")

    override fun findAll(): Collection<Vet> {
        val vets: MutableList<Vet> = ArrayList()
        vets.addAll(
            jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM vets ORDER BY last_name,first_name",
                BeanPropertyRowMapper.newInstance(Vet::class.java)
            )
        )
        val specialties = jdbcTemplate.query(
            "SELECT id, name FROM specialties",
            BeanPropertyRowMapper.newInstance(Specialty::class.java)
        )
        for (vet in vets) {
            val vetSpecialtiesIds = jdbcTemplate.query(
                "SELECT specialty_id FROM vet_specialties WHERE vet_id=?",
                RowMapper { rs: ResultSet, _: Int -> rs.getInt(1) },
                vet.id
            )
            for (specialtyId in vetSpecialtiesIds) {
                val specialty = EntityUtils.getById(ArrayList(specialties), Specialty::class.java, specialtyId)
                vet.addSpecialty(specialty)
            }
        }
        return ArrayList(vets) as Collection<Vet>
    }

    override fun findById(id: Int): Vet {
        return try {
            val vetParams: MutableMap<String, Any> = HashMap()
            vetParams["id"] = id
            val vet = namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name FROM vets WHERE id= :id",
                vetParams,
                BeanPropertyRowMapper.newInstance(Vet::class.java)
            )!!
            val specialties = namedParameterJdbcTemplate.query(
                "SELECT id, name FROM specialties",
                vetParams,
                BeanPropertyRowMapper.newInstance(Specialty::class.java)
            )
            val vetSpecialtiesIds = namedParameterJdbcTemplate.query(
                "SELECT specialty_id FROM vet_specialties WHERE vet_id=:id",
                vetParams,
                RowMapper { rs: ResultSet, _: Int -> rs.getInt(1) }
            )
            for (specialtyId in vetSpecialtiesIds) {
                val specialty = EntityUtils.getById(ArrayList(specialties), Specialty::class.java, specialtyId)
                vet.addSpecialty(specialty)
            }
            vet
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Vet::class.java, id)
        }
    }

    override fun save(vet: Vet) {
        val parameterSource = BeanPropertySqlParameterSource(vet)
        if (vet.isNew()) {
            val newKey = insertVet.executeAndReturnKey(parameterSource)
            vet.id = newKey.toInt()
            updateVetSpecialties(vet)
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE vets SET first_name=:firstName, last_name=:lastName WHERE id=:id",
                parameterSource
            )
            updateVetSpecialties(vet)
        }
    }

    override fun delete(vet: Vet) {
        val params: MutableMap<String, Any> = HashMap()
        params["id"] = vet.id!!
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params)
        namedParameterJdbcTemplate.update("DELETE FROM vets WHERE id=:id", params)
    }

    private fun updateVetSpecialties(vet: Vet) {
        val params: MutableMap<String, Any> = HashMap()
        params["id"] = vet.id!!
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params)
        for (spec in vet.getSpecialties()) {
            params["spec_id"] = spec.id!!
            if (spec.id != null) {
                namedParameterJdbcTemplate.update("INSERT INTO vet_specialties VALUES (:id, :spec_id)", params)
            }
        }
    }
}
