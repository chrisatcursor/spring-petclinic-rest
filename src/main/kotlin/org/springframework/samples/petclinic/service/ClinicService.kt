package org.springframework.samples.petclinic.service

import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.model.Visit

interface ClinicService {
    @Throws(DataAccessException::class)
    fun findPetById(id: Int): Pet?

    @Throws(DataAccessException::class)
    fun findAllPets(): Collection<Pet>

    @Throws(DataAccessException::class)
    fun savePet(pet: Pet)

    @Throws(DataAccessException::class)
    fun deletePet(pet: Pet)

    fun findVisitsByPetId(petId: Int): Collection<Visit>

    @Throws(DataAccessException::class)
    fun findVisitById(visitId: Int): Visit?

    @Throws(DataAccessException::class)
    fun findAllVisits(): Collection<Visit>

    @Throws(DataAccessException::class)
    fun saveVisit(visit: Visit)

    @Throws(DataAccessException::class)
    fun deleteVisit(visit: Visit)

    @Throws(DataAccessException::class)
    fun findVetById(id: Int): Vet?

    @Throws(DataAccessException::class)
    fun findVets(): Collection<Vet>

    @Throws(DataAccessException::class)
    fun findAllVets(): Collection<Vet>

    @Throws(DataAccessException::class)
    fun saveVet(vet: Vet)

    @Throws(DataAccessException::class)
    fun deleteVet(vet: Vet)

    @Throws(DataAccessException::class)
    fun findOwnerById(id: Int): Owner?

    @Throws(DataAccessException::class)
    fun findAllOwners(): Collection<Owner>

    @Throws(DataAccessException::class)
    fun saveOwner(owner: Owner)

    @Throws(DataAccessException::class)
    fun deleteOwner(owner: Owner)

    @Throws(DataAccessException::class)
    fun findOwnerByLastName(lastName: String): Collection<Owner>

    fun findPetTypeById(petTypeId: Int): PetType?

    @Throws(DataAccessException::class)
    fun findAllPetTypes(): Collection<PetType>

    @Throws(DataAccessException::class)
    fun findPetTypes(): Collection<PetType>

    @Throws(DataAccessException::class)
    fun savePetType(petType: PetType)

    @Throws(DataAccessException::class)
    fun deletePetType(petType: PetType)

    fun findSpecialtyById(specialtyId: Int): Specialty?

    @Throws(DataAccessException::class)
    fun findAllSpecialties(): Collection<Specialty>

    @Throws(DataAccessException::class)
    fun saveSpecialty(specialty: Specialty)

    @Throws(DataAccessException::class)
    fun deleteSpecialty(specialty: Specialty)

    @Throws(DataAccessException::class)
    fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty>
}
