package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
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
import java.sql.SQLException
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcVetRepositoryImpl(
    dataSource: DataSource,
    private val jdbcTemplate: JdbcTemplate
) : VetRepository {
    private val insertVet = SimpleJdbcInsert(dataSource).withTableName("vets").usingGeneratedKeyColumns("id")
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Vet> {
        val vets: MutableList<Vet> = arrayListOf()
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
                { rs: ResultSet, _: Int -> rs.getInt(1) },
                vet.id
            )
            for (specialtyId in vetSpecialtiesIds) {
                val specialty = EntityUtils.getById(specialties, Specialty::class.java, specialtyId)
                vet.addSpecialty(specialty)
            }
        }
        return vets
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Vet =
        try {
            val vetParams = hashMapOf<String, Any>("id" to id)
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
                vetParams
            ) { rs: ResultSet, _: Int ->
                try {
                    rs.getInt(1)
                } catch (e: SQLException) {
                    throw RuntimeException(e)
                }
            }
            for (specialtyId in vetSpecialtiesIds) {
                val specialty = EntityUtils.getById(specialties, Specialty::class.java, specialtyId)
                vet.addSpecialty(specialty)
            }
            vet
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Vet::class.java, id)
        }

    @Throws(DataAccessException::class)
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

    @Throws(DataAccessException::class)
    override fun delete(vet: Vet) {
        val params = hashMapOf<String, Any>("id" to vet.id!!)
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params)
        namedParameterJdbcTemplate.update("DELETE FROM vets WHERE id=:id", params)
    }

    @Throws(DataAccessException::class)
    private fun updateVetSpecialties(vet: Vet) {
        val params = hashMapOf<String, Any>("id" to vet.id!!)
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params)
        for (spec in vet.getSpecialties()) {
            params["spec_id"] = spec.id ?: 0
            if (spec.id != null) {
                namedParameterJdbcTemplate.update("INSERT INTO vet_specialties VALUES (:id, :spec_id)", params)
            }
        }
    }
}
