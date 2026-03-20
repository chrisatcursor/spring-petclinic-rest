package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.rest.dto.VisitDto
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto
import org.springframework.stereotype.Component

@Component
class VisitMapper {

    fun toVisit(visitDto: VisitDto): Visit = Visit().apply {
        id = visitDto.id
        description = visitDto.description
        visitDto.date?.let { date = it }
        pet = visitDto.petId?.let { petId ->
            Pet().apply {
                id = petId
            }
        }
    }

    fun toVisit(visitFieldsDto: VisitFieldsDto): Visit = Visit().apply {
        description = visitFieldsDto.description
        visitFieldsDto.date?.let { date = it }
    }

    fun toVisitDto(visit: Visit): VisitDto = VisitDto().apply {
        id = visit.id
        description = visit.description
        date = visit.date
        petId = visit.pet?.id
    }

    fun toVisitsDto(visits: Collection<Visit>): Collection<VisitDto> =
        visits.map { toVisitDto(it) }
}
