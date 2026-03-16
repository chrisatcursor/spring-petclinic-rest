package org.springframework.samples.petclinic.repository.jdbc

import jakarta.transaction.Transactional
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
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
import javax.sql.DataSource

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
class JdbcOwnerRepositoryImpl(dataSource: DataSource) : OwnerRepository {
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    private val insertOwner =
        SimpleJdbcInsert(dataSource)
            .withTableName("owners")
            .usingGeneratedKeyColumns("id")

    @Throws(DataAccessException::class)
    override fun findByLastName(lastName: String): Collection<Owner> {
        val params = hashMapOf<String, Any>("lastName" to "$lastName%")
        val owners = namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like :lastName",
            params,
            BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        loadOwnersPetsAndVisits(owners)
        return owners
    }

    @Throws(DataAccessException::class)
    override fun findById(id: Int): Owner {
        val owner =
            try {
                val params = hashMapOf<String, Any>("id" to id)
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
        val params = hashMapOf<String, Any>("id" to owner.id!!)
        val pets = namedParameterJdbcTemplate.query(
            "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id, visits.id as visit_id, visit_date, description, visits.pet_id as visits_pet_id FROM pets LEFT OUTER JOIN visits ON pets.id = visits.pet_id WHERE owner_id=:id ORDER BY pets.id",
            params,
            JdbcPetVisitExtractor()
        )
        val petTypes = getPetTypes()
        for (pet in pets) {
            pet.type = EntityUtils.getById(petTypes, PetType::class.java, pet.typeId)
            owner.addPet(pet)
        }
    }

    @Throws(DataAccessException::class)
    override fun save(owner: Owner) {
        val parameterSource = BeanPropertySqlParameterSource(owner)
        if (owner.isNew()) {
            val newKey = insertOwner.executeAndReturnKey(parameterSource)
            owner.id = newKey.toInt()
        } else {
            namedParameterJdbcTemplate.update(
                "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, city=:city, telephone=:telephone WHERE id=:id",
                parameterSource
            )
        }
    }

    fun getPetTypes(): Collection<PetType> =
        namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(PetType::class.java)
        )

    private fun loadOwnersPetsAndVisits(owners: List<Owner>) {
        for (owner in owners) {
            loadPetsAndVisits(owner)
        }
    }

    @Throws(DataAccessException::class)
    override fun findAll(): Collection<Owner> {
        val owners = namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners",
            hashMapOf<String, Any>(),
            BeanPropertyRowMapper.newInstance(Owner::class.java)
        )
        for (owner in owners) {
            loadPetsAndVisits(owner)
        }
        return owners
    }

    @Transactional
    @Throws(DataAccessException::class)
    override fun delete(owner: Owner) {
        val ownerParams = hashMapOf<String, Any>("id" to owner.id!!)
        val pets: List<Pet> = owner.getPets()
        for (pet in pets) {
            val petParams = hashMapOf<String, Any>("id" to pet.id!!)
            val visits: List<Visit> = pet.getVisits()
            for (visit in visits) {
                val visitParams = hashMapOf<String, Any>("id" to visit.id!!)
                namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visitParams)
            }
            namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", petParams)
        }
        namedParameterJdbcTemplate.update("DELETE FROM owners WHERE id=:id", ownerParams)
    }
}
