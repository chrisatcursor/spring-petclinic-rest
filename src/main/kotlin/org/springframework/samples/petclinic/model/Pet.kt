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
    private var visits: MutableSet<Visit>? = null

    protected fun getVisitsInternal(): MutableSet<Visit> {
        if (visits == null) {
            visits = mutableSetOf()
        }
        return visits!!
    }

    protected fun setVisitsInternal(visits: Set<Visit>) {
        this.visits = visits.toMutableSet()
    }

    fun getVisits(): List<Visit> = getVisitsInternal().sortedByDescending { it.date }

    fun setVisits(visits: List<Visit>) {
        this.visits = visits.toMutableSet()
    }

    fun addVisit(visit: Visit) {
        getVisitsInternal().add(visit)
        visit.pet = this
    }
}
