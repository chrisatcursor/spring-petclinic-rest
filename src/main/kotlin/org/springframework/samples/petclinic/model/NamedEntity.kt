package org.springframework.samples.petclinic.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotEmpty

@MappedSuperclass
open class NamedEntity : BaseEntity() {
    @Column(name = "name")
    @field:NotEmpty
    var name: String? = null

    override fun toString(): String = name.orEmpty()
}
