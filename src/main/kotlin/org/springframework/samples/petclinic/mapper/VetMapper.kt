package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Vet
import org.springframework.samples.petclinic.rest.dto.VetDto
import org.springframework.samples.petclinic.rest.dto.VetFieldsDto
import org.springframework.stereotype.Component

@Component
class VetMapper(private val specialtyMapper: SpecialtyMapper) {
    fun toVet(vetDto: VetDto): Vet =
        Vet().apply {
            id = vetDto.id
            firstName = vetDto.firstName
            lastName = vetDto.lastName
            setSpecialties(vetDto.specialties?.map { specialtyMapper.toSpecialty(it) } ?: emptyList())
        }

    fun toVet(vetFieldsDto: VetFieldsDto): Vet =
        Vet().apply {
            firstName = vetFieldsDto.firstName
            lastName = vetFieldsDto.lastName
            setSpecialties(vetFieldsDto.specialties?.map { specialtyMapper.toSpecialty(it) } ?: emptyList())
        }

    fun toVetDto(vet: Vet): VetDto =
        VetDto().apply {
            id = vet.id
            firstName = vet.firstName
            lastName = vet.lastName
            specialties = vet.getSpecialties().map { specialtyMapper.toSpecialtyDto(it) }
        }

    fun toVetDtos(vets: Collection<Vet>): Collection<VetDto> = vets.map { toVetDto(it) }
}
