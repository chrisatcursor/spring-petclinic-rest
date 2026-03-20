package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.rest.dto.PetDto
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto
import org.springframework.samples.petclinic.rest.dto.PetTypeDto
import org.springframework.stereotype.Component

@Component
class PetMapper(
    private val visitMapper: VisitMapper
) {

    fun toPetDto(pet: Pet?): PetDto? {
        if (pet == null) {
            return null
        }
        return PetDto().apply {
        id = pet.id
        name = pet.name
        birthDate = pet.birthDate
        type = pet.type?.let { toPetTypeDto(it) }
        ownerId = pet.owner?.id
        setVisits(pet.getVisits().map { visitMapper.toVisitDto(it) })
    }
    }

    fun toPetsDto(pets: Collection<Pet>): Collection<PetDto> =
        pets.mapNotNull { toPetDto(it) }

    fun toPets(pets: Collection<PetDto>): Collection<Pet> =
        pets.mapNotNull { toPet(it) }

    fun toPet(petDto: PetDto?): Pet? {
        if (petDto == null) {
            return null
        }
        return Pet().apply {
        id = petDto.id
        name = petDto.name
        birthDate = petDto.birthDate
        type = petDto.type?.let { toPetType(it) }
        owner = petDto.ownerId?.let { ownerId ->
            Owner().apply {
                id = ownerId
            }
        }
        setVisits(petDto.visits?.map { visitMapper.toVisit(it) } ?: emptyList())
    }
    }

    fun toPet(petFieldsDto: PetFieldsDto): Pet = Pet().apply {
        name = petFieldsDto.name
        birthDate = petFieldsDto.birthDate
        type = petFieldsDto.type?.let { toPetType(it) }
    }

    fun toPetTypeDto(petType: PetType?): PetTypeDto? {
        if (petType == null) {
            return null
        }
        return PetTypeDto().apply {
        id = petType.id
        name = petType.name
    }
    }

    fun toPetType(petTypeDto: PetTypeDto?): PetType? {
        if (petTypeDto == null) {
            return null
        }
        return PetType().apply {
        id = petTypeDto.id
        name = petTypeDto.name
    }
    }

    fun toPetTypeDtos(petTypes: Collection<PetType>): Collection<PetTypeDto> =
        petTypes.mapNotNull { toPetTypeDto(it) }
}
