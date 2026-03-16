package org.springframework.samples.petclinic.model

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "types")
class PetType : NamedEntity()
