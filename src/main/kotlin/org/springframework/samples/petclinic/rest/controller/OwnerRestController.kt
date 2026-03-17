package org.springframework.samples.petclinic.rest.controller

import jakarta.transaction.Transactional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.OwnerMapper
import org.springframework.samples.petclinic.mapper.PetMapper
import org.springframework.samples.petclinic.mapper.VisitMapper
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.rest.api.OwnersApi
import org.springframework.samples.petclinic.rest.dto.OwnerDto
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto
import org.springframework.samples.petclinic.rest.dto.PetDto
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto
import org.springframework.samples.petclinic.rest.dto.VisitDto
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/api"])
class OwnerRestController(
    private val clinicService: ClinicService,
    private val ownerMapper: OwnerMapper,
    private val petMapper: PetMapper,
    private val visitMapper: VisitMapper
) : OwnersApi {
    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun listOwners(lastName: String?): ResponseEntity<List<OwnerDto>> {
        val owners: Collection<Owner> =
            if (lastName != null) {
                clinicService.findOwnerByLastName(lastName)
            } else {
                clinicService.findAllOwners()
            }
        if (owners.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun getOwner(ownerId: Int): ResponseEntity<OwnerDto> {
        val owner = clinicService.findOwnerById(ownerId)
        if (owner == null) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(ownerMapper.toOwnerDto(owner), HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun addOwner(ownerFieldsDto: OwnerFieldsDto): ResponseEntity<OwnerDto> {
        val headers = HttpHeaders()
        val owner = ownerMapper.toOwner(ownerFieldsDto)
        clinicService.saveOwner(owner)
        val ownerDto = ownerMapper.toOwnerDto(owner)
        headers.location = UriComponentsBuilder.newInstance().path("/api/owners/{id}").buildAndExpand(owner.id).toUri()
        return ResponseEntity(ownerDto, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun updateOwner(ownerId: Int, ownerFieldsDto: OwnerFieldsDto): ResponseEntity<OwnerDto> {
        val currentOwner = clinicService.findOwnerById(ownerId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentOwner.address = ownerFieldsDto.address
        currentOwner.city = ownerFieldsDto.city
        currentOwner.firstName = ownerFieldsDto.firstName
        currentOwner.lastName = ownerFieldsDto.lastName
        currentOwner.telephone = ownerFieldsDto.telephone
        clinicService.saveOwner(currentOwner)
        return ResponseEntity(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    override fun deleteOwner(ownerId: Int): ResponseEntity<OwnerDto> {
        val owner = clinicService.findOwnerById(ownerId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        clinicService.deleteOwner(owner)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun addPetToOwner(ownerId: Int, petFieldsDto: PetFieldsDto): ResponseEntity<PetDto> {
        val owner = clinicService.findOwnerById(ownerId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val headers = HttpHeaders()
        val pet = petMapper.toPet(petFieldsDto)
        owner.id = ownerId
        pet.owner = owner
        pet.type?.name = null
        clinicService.savePet(pet)
        val petDto = petMapper.toPetDto(pet)
        headers.location = UriComponentsBuilder.newInstance().path("/api/pets/{id}").buildAndExpand(pet.id).toUri()
        return ResponseEntity(petDto, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun updateOwnersPet(ownerId: Int, petId: Int, petFieldsDto: PetFieldsDto): ResponseEntity<Void> {
        val currentOwner = clinicService.findOwnerById(ownerId)
        if (currentOwner != null) {
            val currentPet = clinicService.findPetById(petId)
            if (currentPet != null) {
                currentPet.birthDate = petFieldsDto.birthDate
                currentPet.name = petFieldsDto.name
                currentPet.type = petFieldsDto.type?.let { petMapper.toPetType(it) }
                clinicService.savePet(currentPet)
                return ResponseEntity(HttpStatus.NO_CONTENT)
            }
        }
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun addVisitToOwner(ownerId: Int, petId: Int, visitFieldsDto: VisitFieldsDto): ResponseEntity<VisitDto> {
        val headers = HttpHeaders()
        val visit = visitMapper.toVisit(visitFieldsDto)
        val pet = Pet()
        pet.id = petId
        visit.pet = pet
        clinicService.saveVisit(visit)
        val visitDto = visitMapper.toVisitDto(visit)
        headers.location = UriComponentsBuilder.newInstance().path("/api/visits/{id}").buildAndExpand(visit.id).toUri()
        return ResponseEntity(visitDto, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun getOwnersPet(ownerId: Int, petId: Int): ResponseEntity<PetDto> {
        val owner = clinicService.findOwnerById(ownerId)
        if (owner != null) {
            val pet = owner.getPet(petId)
            if (pet != null) {
                return ResponseEntity(petMapper.toPetDto(pet), HttpStatus.OK)
            }
        }
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }
}
