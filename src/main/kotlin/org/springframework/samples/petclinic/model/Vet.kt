/*
 * Copyright 2002-2018 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.xml.bind.annotation.XmlElement
import java.util.Collections

@Entity
@Table(name = "vets")
class Vet : Person() {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "vet_specialties",
        joinColumns = [JoinColumn(name = "vet_id")],
        inverseJoinColumns = [JoinColumn(name = "specialty_id")]
    )
    @field:Access(AccessType.FIELD)
    private var specialties: MutableSet<Specialty>? = null

    @JsonIgnore
    protected fun getSpecialtiesInternal(): MutableSet<Specialty> {
        if (specialties == null) {
            specialties = HashSet()
        }
        return specialties!!
    }

    protected fun setSpecialtiesInternal(specialties: MutableSet<Specialty>?) {
        this.specialties = specialties
    }

    @XmlElement
    fun getSpecialties(): List<Specialty> {
        val sortedSpecs = ArrayList(getSpecialtiesInternal())
        sortedSpecs.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name ?: "" })
        return Collections.unmodifiableList(sortedSpecs)
    }

    fun setSpecialties(specialties: List<Specialty>) {
        this.specialties = HashSet(specialties)
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
