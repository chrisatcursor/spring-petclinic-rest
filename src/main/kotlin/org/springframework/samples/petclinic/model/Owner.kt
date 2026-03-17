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
    var address: String? = null

    @Column(name = "city")
    @field:NotEmpty
    var city: String? = null

    @Column(name = "telephone")
    @field:NotEmpty
    @field:Digits(fraction = 0, integer = 10)
    @field:Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    var telephone: String? = null

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "owner", fetch = FetchType.EAGER)
    private var pets: MutableSet<Pet>? = null

    protected fun getPetsInternal(): MutableSet<Pet> {
        if (pets == null) {
            pets = mutableSetOf()
        }
        return pets!!
    }

    protected fun setPetsInternal(pets: Set<Pet>) {
        this.pets = pets.toMutableSet()
    }

    fun getPets(): List<Pet> = getPetsInternal().sortedBy { it.name.orEmpty().lowercase() }

    fun setPets(pets: List<Pet>) {
        this.pets = pets.toMutableSet()
    }

    fun addPet(pet: Pet) {
        getPetsInternal().add(pet)
        pet.owner = this
    }

    fun getPet(name: String): Pet? = getPet(name, false)

    fun getPet(name: String, ignoreNew: Boolean): Pet? {
        val loweredName = name.lowercase()
        for (pet in getPetsInternal()) {
            if (!ignoreNew || !pet.isNew()) {
                if (pet.name.orEmpty().lowercase() == loweredName) {
                    return pet
                }
            }
        }
        return null
    }

    fun getPet(petId: Int?): Pet? = getPetsInternal().firstOrNull { it.id == petId }

    override fun toString(): String =
        ToStringCreator(this)
            .append("id", id)
            .append("new", isNew())
            .append("lastName", lastName)
            .append("firstName", firstName)
            .append("address", address)
            .append("city", city)
            .append("telephone", telephone)
            .toString()
}
