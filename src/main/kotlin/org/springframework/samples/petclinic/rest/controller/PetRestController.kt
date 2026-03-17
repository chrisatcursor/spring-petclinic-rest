package org.springframework.samples.petclinic.rest.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.PetMapper
import org.springframework.samples.petclinic.rest.api.PetsApi
import org.springframework.samples.petclinic.rest.dto.PetDto
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/api"])
class PetRestController(
    private val clinicService: ClinicService,
    private val petMapper: PetMapper
) : PetsApi {
    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun getPet(petId: Int): ResponseEntity<PetDto> {
        val pet = petMapper.toPetDto(clinicService.findPetById(petId))
        if (pet == null) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(pet, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun listPets(): ResponseEntity<List<PetDto>> {
        val pets = arrayListOf<PetDto>()
        pets.addAll(petMapper.toPetsDto(clinicService.findAllPets()))
        if (pets.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(pets, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun updatePet(petId: Int, petDto: PetDto): ResponseEntity<PetDto> {
        val currentPet = clinicService.findPetById(petId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentPet.birthDate = petDto.birthDate
        currentPet.name = petDto.name
        currentPet.type = petDto.type?.let { petMapper.toPetType(it) }
        clinicService.savePet(currentPet)
        return ResponseEntity(petMapper.toPetDto(currentPet), HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun deletePet(petId: Int): ResponseEntity<PetDto> {
        val pet = clinicService.findPetById(petId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        clinicService.deletePet(pet)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
