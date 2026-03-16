package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.rest.dto.OwnerDto
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto
import org.springframework.stereotype.Component

@Component
class OwnerMapper(private val petMapper: PetMapper) {
    fun toOwnerDto(owner: Owner): OwnerDto =
        OwnerDto().apply {
            id = owner.id
            firstName = owner.firstName
            lastName = owner.lastName
            address = owner.address
            city = owner.city
            telephone = owner.telephone
            pets = owner.getPets().mapNotNull { petMapper.toPetDto(it) }
        }

    fun toOwner(ownerDto: OwnerDto): Owner =
        Owner().apply {
            id = ownerDto.id
            firstName = ownerDto.firstName
            lastName = ownerDto.lastName
            address = ownerDto.address
            city = ownerDto.city
            telephone = ownerDto.telephone
            setPets(ownerDto.pets?.map { petMapper.toPet(it).also { mapped -> mapped.owner = this } } ?: emptyList())
        }

    fun toOwner(ownerDto: OwnerFieldsDto): Owner =
        Owner().apply {
            firstName = ownerDto.firstName
            lastName = ownerDto.lastName
            address = ownerDto.address
            city = ownerDto.city
            telephone = ownerDto.telephone
        }

    fun toOwnerDtoCollection(ownerCollection: Collection<Owner>): List<OwnerDto> = ownerCollection.map { toOwnerDto(it) }

    fun toOwners(ownerDtos: Collection<OwnerDto>): Collection<Owner> = ownerDtos.map { toOwner(it) }
}
