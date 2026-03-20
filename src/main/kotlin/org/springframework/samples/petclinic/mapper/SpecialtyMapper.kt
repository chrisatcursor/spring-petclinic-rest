package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto
import org.springframework.stereotype.Component

@Component
class SpecialtyMapper {

    fun toSpecialty(specialtyDto: SpecialtyDto): Specialty = Specialty().apply {
        id = specialtyDto.id
        name = specialtyDto.name
    }

    fun toSpecialtyDto(specialty: Specialty): SpecialtyDto = SpecialtyDto().apply {
        id = specialty.id
        name = specialty.name
    }

    fun toSpecialtyDtos(specialties: Collection<Specialty>): Collection<SpecialtyDto> =
        specialties.map { toSpecialtyDto(it) }

    fun toSpecialtys(specialties: Collection<SpecialtyDto>): Collection<Specialty> =
        specialties.map { toSpecialty(it) }
}
