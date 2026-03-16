package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.rest.dto.PetDto
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto
import org.springframework.samples.petclinic.rest.dto.PetTypeDto
import org.springframework.stereotype.Component

@Component
class PetMapper(private val visitMapper: VisitMapper) {
    fun toPetDto(pet: Pet?): PetDto? {
        pet ?: return null
        return PetDto().apply {
            id = pet.id
            name = pet.name
            birthDate = pet.birthDate
            type = pet.type?.let { toPetTypeDto(it) }
            ownerId = pet.owner?.id
            visits = toVisitsDtoList(pet.getVisits())
        }
    }

    fun toPetsDto(pets: Collection<Pet>): Collection<PetDto> = pets.mapNotNull { toPetDto(it) }

    fun toPets(pets: Collection<PetDto>): Collection<Pet> = pets.map { toPet(it) }

    fun toPet(petDto: PetDto): Pet =
        Pet().apply {
            id = petDto.id
            name = petDto.name
            birthDate = petDto.birthDate
            type = petDto.type?.let { toPetType(it) }
            owner = petDto.ownerId?.let { Owner().apply { id = it } }
            setVisits(petDto.visits?.map { visitMapper.toVisit(it).also { mapped -> mapped.pet = this } } ?: emptyList())
        }

    fun toPet(petFieldsDto: PetFieldsDto): Pet =
        Pet().apply {
            name = petFieldsDto.name
            birthDate = petFieldsDto.birthDate
            type = petFieldsDto.type?.let { toPetType(it) }
        }

    fun toPetTypeDto(petType: PetType): PetTypeDto =
        PetTypeDto().apply {
            id = petType.id
            name = petType.name
        }

    fun toPetType(petTypeDto: PetTypeDto): PetType =
        PetType().apply {
            id = petTypeDto.id
            name = petTypeDto.name
        }

    fun toPetTypeDtos(petTypes: Collection<PetType>): Collection<PetTypeDto> = petTypes.map { toPetTypeDto(it) }

    private fun toVisitsDtoList(visits: Collection<org.springframework.samples.petclinic.model.Visit>): List<org.springframework.samples.petclinic.rest.dto.VisitDto> =
        visitMapper.toVisitsDto(visits).toList()
}
