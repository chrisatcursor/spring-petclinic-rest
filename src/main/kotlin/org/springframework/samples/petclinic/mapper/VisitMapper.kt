package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.rest.dto.VisitDto
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto
import org.springframework.stereotype.Component

@Component
class VisitMapper {
    fun toVisit(visitDto: VisitDto): Visit =
        Visit().apply {
            id = visitDto.id
            date = visitDto.date
            description = visitDto.description
            pet = Pet().apply { id = visitDto.petId }
        }

    fun toVisit(visitFieldsDto: VisitFieldsDto): Visit =
        Visit().apply {
            date = visitFieldsDto.date
            description = visitFieldsDto.description
        }

    fun toVisitDto(visit: Visit): VisitDto =
        VisitDto().apply {
            id = visit.id
            date = visit.date
            description = visit.description
            petId = visit.pet?.id
        }

    fun toVisitsDto(visits: Collection<Visit>): Collection<VisitDto> = visits.map { toVisitDto(it) }
}
