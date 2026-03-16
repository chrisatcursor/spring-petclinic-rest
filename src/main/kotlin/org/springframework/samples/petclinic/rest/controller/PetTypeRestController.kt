package org.springframework.samples.petclinic.rest.controller

import jakarta.transaction.Transactional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.PetTypeMapper
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.rest.api.PettypesApi
import org.springframework.samples.petclinic.rest.dto.PetTypeDto
import org.springframework.samples.petclinic.rest.dto.PetTypeFieldsDto
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/api"])
class PetTypeRestController(
    private val clinicService: ClinicService,
    private val petTypeMapper: PetTypeMapper
) : PettypesApi {
    @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
    override fun listPetTypes(): ResponseEntity<List<PetTypeDto>> {
        val petTypes = arrayListOf<PetType>()
        petTypes.addAll(clinicService.findAllPetTypes())
        if (petTypes.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(petTypeMapper.toPetTypeDtos(petTypes), HttpStatus.OK)
    }

    @PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
    override fun getPetType(petTypeId: Int): ResponseEntity<PetTypeDto> {
        val petType = clinicService.findPetTypeById(petTypeId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(petTypeMapper.toPetTypeDto(petType), HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun addPetType(petTypeFieldsDto: PetTypeFieldsDto): ResponseEntity<PetTypeDto> {
        val headers = HttpHeaders()
        val type = petTypeMapper.toPetType(petTypeFieldsDto)
        clinicService.savePetType(type)
        headers.location = UriComponentsBuilder.newInstance().path("/api/pettypes/{id}").buildAndExpand(type.id).toUri()
        return ResponseEntity(petTypeMapper.toPetTypeDto(type), headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun updatePetType(petTypeId: Int, petTypeDto: PetTypeDto): ResponseEntity<PetTypeDto> {
        val currentPetType = clinicService.findPetTypeById(petTypeId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentPetType.name = petTypeDto.name
        clinicService.savePetType(currentPetType)
        return ResponseEntity(petTypeMapper.toPetTypeDto(currentPetType), HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    override fun deletePetType(petTypeId: Int): ResponseEntity<PetTypeDto> {
        val petType = clinicService.findPetTypeById(petTypeId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        clinicService.deletePetType(petType)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
