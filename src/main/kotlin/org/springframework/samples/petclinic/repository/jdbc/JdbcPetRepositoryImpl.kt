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
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcPetRepositoryImpl(
    dataSource: DataSource,
    private val ownerRepository: OwnerRepository
) : PetRepository {
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    private val insertPet =
        SimpleJdbcInsert(dataSource)
            .withTableName("pets")
            .usingGeneratedKeyColumns("id")

    @Throws(DataAccessException::class)
    override fun findPetTypes(): List<PetType> =
        namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(PetType::class.java)
        )

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Pet {
        val ownerId =
            try {
                val params = hashMapOf<String, Any>("id" to id)
                namedParameterJdbcTemplate.queryForObject("SELECT owner_id FROM pets WHERE id=:id", params, Int::class.java)!!
            } catch (ex: EmptyResultDataAccessException) {
                throw ObjectRetrievalFailureException(Pet::class.java, id)
            }
        val owner = ownerRepository.findById(ownerId)
        return EntityUtils.getById(owner.getPets(), Pet::class.java, id)
    }

    @Throws(DataAccessException::class)
    override fun save(pet: Pet) {
        if (pet.isNew()) {
            val newKey = insertPet.executeAndReturnKey(createPetParameterSource(pet))
            pet.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE pets SET name=:name, birth_date=:birth_date, type_id=:type_id, owner_id=:owner_id WHERE id=:id",
                createPetParameterSource(pet)
            )
        }
    }

    private fun createPetParameterSource(pet: Pet): MapSqlParameterSource =
        MapSqlParameterSource()
            .addValue("id", pet.id)
            .addValue("name", pet.name)
            .addValue("birth_date", pet.birthDate)
            .addValue("type_id", pet.type?.id)
            .addValue("owner_id", pet.owner?.id)

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Pet> {
        val params = hashMapOf<String, Any>()
        val pets: MutableCollection<Pet> = arrayListOf()
        val jdbcPets = namedParameterJdbcTemplate.query(
            "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id FROM pets",
            params,
            JdbcPetRowMapper()
        )
        val petTypes = namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(PetType::class.java)
        )
        val owners = namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners ORDER BY last_name",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        for (jdbcPet in jdbcPets) {
            jdbcPet.type = EntityUtils.getById(petTypes, PetType::class.java, jdbcPet.typeId)
            jdbcPet.owner = EntityUtils.getById(owners, Owner::class.java, jdbcPet.ownerId)
            pets.add(jdbcPet)
        }
        return pets
    }

    @Throws(DataAccessException::class)
    override fun delete(pet: Pet) {
        val petParams = hashMapOf<String, Any>("id" to pet.id!!)
        val visits: List<Visit> = pet.getVisits()
        for (visit in visits) {
            val visitParams = hashMapOf<String, Any>("id" to visit.id!!)
            namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visitParams)
        }
        namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", petParams)
    }
}
