package org.springframework.samples.petclinic.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotEmpty

@MappedSuperclass
open class Person : BaseEntity() {
    @Column(name = "first_name")
    @field:NotEmpty
    var firstName: String? = null

    @Column(name = "last_name")
    @field:NotEmpty
    var lastName: String? = null
}
