package org.springframework.samples.petclinic.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate

@Entity
@Table(name = "visits")
class Visit : BaseEntity() {
    @Column(name = "visit_date", columnDefinition = "DATE")
    var date: LocalDate? = LocalDate.now()

    @field:NotEmpty
    @Column(name = "description")
    var description: String? = null

    @ManyToOne
    @JoinColumn(name = "pet_id")
    var pet: Pet? = null
}
