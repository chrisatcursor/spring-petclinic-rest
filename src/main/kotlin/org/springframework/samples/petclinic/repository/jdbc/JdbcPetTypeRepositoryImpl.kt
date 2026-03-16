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
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcPetTypeRepositoryImpl(dataSource: DataSource) : PetTypeRepository {
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    private val insertPetType =
        SimpleJdbcInsert(dataSource)
            .withTableName("types")
            .usingGeneratedKeyColumns("id")

    override fun findById(id: Int): PetType =
        try {
            val params = hashMapOf<String, Any>("id" to id)
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(PetType::class.java, id)
        }

    @Throws(DataAccessException::class)
    override fun findByName(name: String): PetType =
        try {
            val params = hashMapOf<String, Any>("name" to name)
            namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM types WHERE name= :name",
                params,
                BeanPropertyRowMapper.newInstance(PetType::class.java)
            )!!
        } catch (ex: EmptyResultDataAccessException) {
            throw ObjectRetrievalFailureException(PetType::class.java, name)
        }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<PetType> =
        namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(PetType::class.java)
        )

    @Throws(DataAccessException::class)
    override fun save(petType: PetType) {
        val parameterSource = BeanPropertySqlParameterSource(petType)
        if (petType.isNew()) {
            val newKey = insertPetType.executeAndReturnKey(parameterSource)
            petType.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update("UPDATE types SET name=:name WHERE id=:id", parameterSource)
        }
    }

    @Throws(DataAccessException::class)
    override fun delete(petType: PetType) {
        val pettypeParams = hashMapOf<String, Any>("id" to petType.id!!)
        val pets = namedParameterJdbcTemplate.query(
            "SELECT pets.id, name, birth_date, type_id, owner_id FROM pets WHERE type_id=:id",
            pettypeParams,
            BeanPropertyRowMapper.newInstance(Pet::class.java)
        )
        for (pet in pets) {
            val petParams = hashMapOf<String, Any>("id" to pet.id!!)
            val visits = namedParameterJdbcTemplate.query(
                "SELECT id, pet_id, visit_date, description FROM visits WHERE pet_id = :id",
                petParams,
                BeanPropertyRowMapper.newInstance(Visit::class.java)
            )
            for (visit in visits) {
                val visitParams = hashMapOf<String, Any>("id" to visit.id!!)
                namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visitParams)
            }
            namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", petParams)
        }
        namedParameterJdbcTemplate.update("DELETE FROM types WHERE id=:id", pettypeParams)
    }
}
