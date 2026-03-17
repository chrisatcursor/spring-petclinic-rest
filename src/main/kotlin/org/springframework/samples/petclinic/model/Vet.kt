package org.springframework.samples.petclinic.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.xml.bind.annotation.XmlElement

@Entity
@Table(name = "vets")
class Vet : Person() {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "vet_specialties",
        joinColumns = [JoinColumn(name = "vet_id")],
        inverseJoinColumns = [JoinColumn(name = "specialty_id")]
    )
    private var specialties: MutableSet<Specialty>? = null

    @JsonIgnore
    protected fun getSpecialtiesInternal(): MutableSet<Specialty> {
        if (specialties == null) {
            specialties = mutableSetOf()
        }
        return specialties!!
    }

    protected fun setSpecialtiesInternal(specialties: Set<Specialty>) {
        this.specialties = specialties.toMutableSet()
    }

    @XmlElement
    fun getSpecialties(): List<Specialty> = getSpecialtiesInternal().sortedBy { it.name.orEmpty().lowercase() }

    fun setSpecialties(specialties: List<Specialty>) {
        this.specialties = specialties.toMutableSet()
    }

    @JsonIgnore
    fun getNrOfSpecialties(): Int = getSpecialtiesInternal().size

    fun addSpecialty(specialty: Specialty) {
        getSpecialtiesInternal().add(specialty)
    }

    fun clearSpecialties() {
        getSpecialtiesInternal().clear()
    }
}
