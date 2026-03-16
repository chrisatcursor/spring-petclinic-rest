package org.springframework.samples.petclinic.rest.controller

import jakarta.transaction.Transactional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.SpecialtyMapper
import org.springframework.samples.petclinic.mapper.VetMapper
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.rest.api.VetsApi
import org.springframework.samples.petclinic.rest.dto.VetDto
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/api"])
class VetRestController(
    private val clinicService: ClinicService,
    private val vetMapper: VetMapper,
    private val specialtyMapper: SpecialtyMapper
) : VetsApi {
    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun listVets(): ResponseEntity<List<VetDto>> {
        val vets = arrayListOf<VetDto>()
        vets.addAll(vetMapper.toVetDtos(clinicService.findAllVets()))
        if (vets.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(vets, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun getVet(vetId: Int): ResponseEntity<VetDto> {
        val vet = clinicService.findVetById(vetId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(vetMapper.toVetDto(vet), HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun addVet(vetDto: VetDto): ResponseEntity<VetDto> {
        val headers = HttpHeaders()
        val vet = vetMapper.toVet(vetDto)
        if (vet.getNrOfSpecialties() > 0) {
            val vetSpecialities =
                clinicService.findSpecialtiesByNameIn(vet.getSpecialties().map { it.name.orEmpty() }.toSet())
            vet.setSpecialties(vetSpecialities)
        }
        clinicService.saveVet(vet)
        headers.location = UriComponentsBuilder.newInstance().path("/api/vets/{id}").buildAndExpand(vet.id).toUri()
        return ResponseEntity(vetMapper.toVetDto(vet), headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    override fun updateVet(vetId: Int, vetDto: VetDto): ResponseEntity<VetDto> {
        val currentVet = clinicService.findVetById(vetId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentVet.firstName = vetDto.firstName
        currentVet.lastName = vetDto.lastName
        currentVet.clearSpecialties()
        for (spec: Specialty in specialtyMapper.toSpecialtys(vetDto.specialties.orEmpty())) {
            currentVet.addSpecialty(spec)
        }
        if (currentVet.getNrOfSpecialties() > 0) {
            val vetSpecialities =
                clinicService.findSpecialtiesByNameIn(currentVet.getSpecialties().map { it.name.orEmpty() }.toSet())
            currentVet.setSpecialties(vetSpecialities)
        }
        clinicService.saveVet(currentVet)
        return ResponseEntity(vetMapper.toVetDto(currentVet), HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Transactional
    override fun deleteVet(vetId: Int): ResponseEntity<VetDto> {
        val vet = clinicService.findVetById(vetId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        clinicService.deleteVet(vet)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
