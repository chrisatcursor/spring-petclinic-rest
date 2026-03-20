/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import org.springframework.core.style.ToStringCreator

@Entity
@Table(name = "owners")
class Owner : Person() {

    @Column(name = "address")
    @field:NotEmpty
    var address: String = ""

    @Column(name = "city")
    @field:NotEmpty
    var city: String = ""

    @Column(name = "telephone")
    @field:NotEmpty
    @field:Digits(fraction = 0, integer = 10)
    @field:Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    var telephone: String = ""

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "owner", fetch = FetchType.EAGER)
    private var pets: MutableSet<Pet>? = null

    protected fun getPetsInternal(): MutableSet<Pet> {
        if (pets == null) {
            pets = mutableSetOf()
        }
        return pets!!
    }

    protected fun setPetsInternal(newPets: MutableSet<Pet>) {
        pets = newPets
    }

    fun getPets(): List<Pet> {
        val sortedPets = getPetsInternal().toMutableList()
        sortedPets.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name ?: "" })
        return sortedPets.toList()
    }

    fun setPets(newPets: List<Pet>) {
        pets = newPets.toMutableSet()
    }

    fun addPet(pet: Pet) {
        getPetsInternal().add(pet)
        pet.owner = this
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     */
    fun getPet(name: String): Pet? = getPet(name, false)

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     */
    fun getPet(name: String, ignoreNew: Boolean): Pet? {
        val lowerName = name.lowercase()
        for (pet in getPetsInternal()) {
            if (!ignoreNew || !pet.isNew()) {
                if ((pet.name ?: "").lowercase() == lowerName) {
                    return pet
                }
            }
        }
        return null
    }

    fun getPet(petId: Int?): Pet? =
        getPetsInternal().firstOrNull { it.id == petId }

    override fun toString(): String = ToStringCreator(this)
        .append("id", id)
        .append("new", isNew())
        .append("lastName", lastName)
        .append("firstName", firstName)
        .append("address", address)
        .append("city", city)
        .append("telephone", telephone)
        .toString()
}
