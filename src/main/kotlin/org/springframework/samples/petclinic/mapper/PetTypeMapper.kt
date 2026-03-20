package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.rest.dto.PetTypeDto
import org.springframework.samples.petclinic.rest.dto.PetTypeFieldsDto
import org.springframework.stereotype.Component

@Component
class PetTypeMapper {

    fun toPetType(petTypeDto: PetTypeDto): PetType = PetType().apply {
        id = petTypeDto.id
        name = petTypeDto.name
    }

    fun toPetType(petTypeFieldsDto: PetTypeFieldsDto): PetType = PetType().apply {
        name = petTypeFieldsDto.name
    }

    fun toPetTypeDto(petType: PetType): PetTypeDto = PetTypeDto().apply {
        id = petType.id
        name = petType.name
    }

    fun toPetTypeFieldsDto(petType: PetType): PetTypeFieldsDto = PetTypeFieldsDto().apply {
        name = petType.name
    }

    fun toPetTypeDtos(petTypes: Collection<PetType>): List<PetTypeDto> =
        petTypes.map { toPetTypeDto(it) }
}
