package org.springframework.samples.petclinic.rest.controller

import jakarta.transaction.Transactional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.SpecialtyMapper
import org.springframework.samples.petclinic.rest.api.SpecialtiesApi
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/api"])
class SpecialtyRestController(
    private val clinicService: ClinicService,
    private val specialtyMapper: SpecialtyMapper
) : SpecialtiesApi {
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun listSpecialties(): ResponseEntity<List<SpecialtyDto>> {
        val specialties = arrayListOf<SpecialtyDto>()
        specialties.addAll(specialtyMapper.toSpecialtyDtos(clinicService.findAllSpecialties()))
        if (specialties.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(specialties, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun getSpecialty(specialtyId: Int): ResponseEntity<SpecialtyDto> {
        val specialty = clinicService.findSpecialtyById(specialtyId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(specialtyMapper.toSpecialtyDto(specialty), HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun addSpecialty(specialtyDto: SpecialtyDto): ResponseEntity<SpecialtyDto> {
        val headers = HttpHeaders()
        val specialty = specialtyMapper.toSpecialty(specialtyDto)
        clinicService.saveSpecialty(specialty)
        headers.location = UriComponentsBuilder.newInstance().path("/api/specialties/{id}").buildAndExpand(specialty.id).toUri()
        return ResponseEntity(specialtyMapper.toSpecialtyDto(specialty), headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun updateSpecialty(specialtyId: Int, specialtyDto: SpecialtyDto): ResponseEntity<SpecialtyDto> {
        val currentSpecialty = clinicService.findSpecialtyById(specialtyId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentSpecialty.name = specialtyDto.name
        clinicService.saveSpecialty(currentSpecialty)
        return ResponseEntity(specialtyMapper.toSpecialtyDto(currentSpecialty), HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    override fun deleteSpecialty(specialtyId: Int): ResponseEntity<SpecialtyDto> {
        val specialty = clinicService.findSpecialtyById(specialtyId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        clinicService.deleteSpecialty(specialty)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
