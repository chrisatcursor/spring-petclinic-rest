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
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.stereotype.Repository
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcSpecialtyRepositoryImpl(dataSource: DataSource) : SpecialtyRepository {
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    private val insertSpecialty =
        SimpleJdbcInsert(dataSource)
            .withTableName("specialties")
            .usingGeneratedKeyColumns("id")

    override fun findById(id: Int): Specialty =
        try {
            val params = hashMapOf<String, Any>("id" to id)
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM specialties WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(Specialty::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Specialty::class.java, id)
        }

    override fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty> =
        try {
            val sql = "SELECT id, name FROM specialties WHERE specialties.name IN (:names)"
            val params = hashMapOf<String, Any>("names" to names)
            namedParameterJdbcTemplate.query(
                sql,
                params,
                BeanPropertyRowMapper(Specialty::class.java)
            )
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(Specialty::class.java, names)
        }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Specialty> =
        namedParameterJdbcTemplate.query(
            "SELECT id, name FROM specialties",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(Specialty::class.java)
        )

    @Throws(DataAccessException::class)
    override fun save(specialty: Specialty) {
        val parameterSource = BeanPropertySqlParameterSource(specialty)
        if (specialty.isNew()) {
            val newKey = insertSpecialty.executeAndReturnKey(parameterSource)
            specialty.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update("UPDATE specialties SET name=:name WHERE id=:id", parameterSource)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(specialty: Specialty) {
        val params = hashMapOf<String, Any>("id" to specialty.id!!)
        namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE specialty_id=:id", params)
        namedParameterJdbcTemplate.update("DELETE FROM specialties WHERE id=:id", params)
    }
}
