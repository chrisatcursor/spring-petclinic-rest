package org.springframework.samples.petclinic.rest.controller

import jakarta.transaction.Transactional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.VisitMapper
import org.springframework.samples.petclinic.rest.api.VisitsApi
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
class VisitRestController(
    private val clinicService: ClinicService,
    private val visitMapper: VisitMapper
) : VisitsApi {
    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun listVisits(): ResponseEntity<List<VisitDto>> {
        val visits = arrayListOf(clinicService.findAllVisits()).flatten()
        if (visits.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(arrayListOf<VisitDto>().apply { addAll(visitMapper.toVisitsDto(visits)) }, HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun getVisit(visitId: Int): ResponseEntity<VisitDto> {
        val visit = clinicService.findVisitById(visitId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(visitMapper.toVisitDto(visit), HttpStatus.OK)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun addVisit(visitDto: VisitDto): ResponseEntity<VisitDto> {
        val headers = HttpHeaders()
        val visit = visitMapper.toVisit(visitDto)
        clinicService.saveVisit(visit)
        val savedVisitDto = visitMapper.toVisitDto(visit)
        headers.location = UriComponentsBuilder.newInstance().path("/api/visits/{id}").buildAndExpand(visit.id).toUri()
        return ResponseEntity(savedVisitDto, headers, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    override fun updateVisit(visitId: Int, visitDto: VisitFieldsDto): ResponseEntity<VisitDto> {
        val currentVisit = clinicService.findVisitById(visitId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentVisit.date = visitDto.date
        currentVisit.description = visitDto.description
        clinicService.saveVisit(currentVisit)
        return ResponseEntity(visitMapper.toVisitDto(currentVisit), HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    override fun deleteVisit(visitId: Int): ResponseEntity<VisitDto> {
        val visit = clinicService.findVisitById(visitId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        clinicService.deleteVisit(visit)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
