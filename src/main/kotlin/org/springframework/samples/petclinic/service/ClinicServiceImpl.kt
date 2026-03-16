package org.springframework.samples.petclinic.service

import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.orm.ObjectRetrievalFailureException
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.repository.OwnerRepository
import org.springframework.samples.petclinic.repository.PetRepository
import org.springframework.samples.petclinic.repository.PetTypeRepository
import org.springframework.samples.petclinic.repository.SpecialtyRepository
import org.springframework.samples.petclinic.repository.VetRepository
import org.springframework.samples.petclinic.repository.VisitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClinicServiceImpl(
    private val petRepository: PetRepository,
    private val vetRepository: VetRepository,
    private val ownerRepository: OwnerRepository,
    private val visitRepository: VisitRepository,
    private val specialtyRepository: SpecialtyRepository,
    private val petTypeRepository: PetTypeRepository
) : ClinicService {
    @Transactional(readOnly = true)
    override fun findAllPets(): Collection<Pet> = petRepository.findAll()

    @Transactional
    override fun deletePet(pet: Pet) {
        petRepository.delete(pet)
    }

    @Transactional(readOnly = true)
    override fun findVisitById(visitId: Int): Visit? = findEntityById { visitRepository.findById(visitId) }

    @Transactional(readOnly = true)
    override fun findAllVisits(): Collection<Visit> = visitRepository.findAll()

    @Transactional
    override fun deleteVisit(visit: Visit) {
        visitRepository.delete(visit)
    }

    @Transactional(readOnly = true)
    override fun findVetById(id: Int): Vet? = findEntityById { vetRepository.findById(id) }

    @Transactional(readOnly = true)
    override fun findAllVets(): Collection<Vet> = vetRepository.findAll()

    @Transactional
    override fun saveVet(vet: Vet) {
        vetRepository.save(vet)
    }

    @Transactional
    override fun deleteVet(vet: Vet) {
        vetRepository.delete(vet)
    }

    @Transactional(readOnly = true)
    override fun findAllOwners(): Collection<Owner> = ownerRepository.findAll()

    @Transactional
    override fun deleteOwner(owner: Owner) {
        ownerRepository.delete(owner)
    }

    @Transactional(readOnly = true)
    override fun findPetTypeById(petTypeId: Int): PetType? = findEntityById { petTypeRepository.findById(petTypeId) }

    @Transactional(readOnly = true)
    override fun findAllPetTypes(): Collection<PetType> = petTypeRepository.findAll()

    @Transactional
    override fun savePetType(petType: PetType) {
        petTypeRepository.save(petType)
    }

    @Transactional
    override fun deletePetType(petType: PetType) {
        petTypeRepository.delete(petType)
    }

    @Transactional(readOnly = true)
    override fun findSpecialtyById(specialtyId: Int): Specialty? = findEntityById { specialtyRepository.findById(specialtyId) }

    @Transactional(readOnly = true)
    override fun findAllSpecialties(): Collection<Specialty> = specialtyRepository.findAll()

    @Transactional
    override fun saveSpecialty(specialty: Specialty) {
        specialtyRepository.save(specialty)
    }

    @Transactional
    override fun deleteSpecialty(specialty: Specialty) {
        specialtyRepository.delete(specialty)
    }

    @Transactional(readOnly = true)
    override fun findPetTypes(): Collection<PetType> = petRepository.findPetTypes()

    @Transactional(readOnly = true)
    override fun findOwnerById(id: Int): Owner? = findEntityById { ownerRepository.findById(id) }

    @Transactional(readOnly = true)
    override fun findPetById(id: Int): Pet? = findEntityById { petRepository.findById(id) }

    @Transactional
    override fun savePet(pet: Pet) {
        pet.type = pet.type?.id?.let { findPetTypeById(it) }
        petRepository.save(pet)
    }

    @Transactional
    override fun saveVisit(visit: Visit) {
        visitRepository.save(visit)
    }

    @Transactional(readOnly = true)
    override fun findVets(): Collection<Vet> = vetRepository.findAll()

    @Transactional
    override fun saveOwner(owner: Owner) {
        ownerRepository.save(owner)
    }

    @Transactional(readOnly = true)
    override fun findOwnerByLastName(lastName: String): Collection<Owner> = ownerRepository.findByLastName(lastName)

    @Transactional(readOnly = true)
    override fun findVisitsByPetId(petId: Int): Collection<Visit> = visitRepository.findByPetId(petId)

    @Transactional(readOnly = true)
    override fun findSpecialtiesByNameIn(names: Set<String>): List<Specialty> = specialtyRepository.findSpecialtiesByNameIn(names)

    private fun <T> findEntityById(supplier: () -> T): T? =
        try {
            supplier()
        } catch (_: ObjectRetrievalFailureException) {
            null
        } catch (_: EmptyResultDataAccessException) {
            null
        }
}
