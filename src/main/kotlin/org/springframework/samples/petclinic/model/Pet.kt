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
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * Simple business object representing a pet.
 */
@Entity
@Table(name = "pets")
open class Pet : NamedEntity() {

    @Column(name = "birth_date", columnDefinition = "DATE")
    var birthDate: LocalDate? = null

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "type_id")
    var type: PetType? = null

    @ManyToOne
    @JoinColumn(name = "owner_id")
    var owner: Owner? = null

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "pet", fetch = FetchType.EAGER)
    private var _visits: MutableSet<Visit>? = null

    protected fun getVisitsInternal(): MutableSet<Visit> {
        if (_visits == null) {
            _visits = mutableSetOf()
        }
        return _visits!!
    }

    protected fun setVisitsInternal(visits: MutableSet<Visit>) {
        _visits = visits
    }

    fun getVisits(): List<Visit> {
        val sortedVisits = getVisitsInternal().toMutableList()
        sortedVisits.sortWith(compareByDescending<Visit> { it.date })
        return sortedVisits.toList()
    }

    fun setVisits(visits: List<Visit>) {
        _visits = visits.toMutableSet()
    }

    fun addVisit(visit: Visit) {
        getVisitsInternal().add(visit)
        visit.pet = this
    }
}
