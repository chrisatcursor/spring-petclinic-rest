package org.springframework.samples.petclinic.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "roles",
    uniqueConstraints = [UniqueConstraint(columnNames = ["username", "role"])]
)
class Role : BaseEntity() {

    @ManyToOne
    @JoinColumn(name = "username")
    @JsonIgnore
    var user: User? = null

    @Column(name = "role")
    var name: String? = null
}
