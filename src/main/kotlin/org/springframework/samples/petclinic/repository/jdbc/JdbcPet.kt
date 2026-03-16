package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.samples.petclinic.model.Pet

class JdbcPet : Pet() {
    var typeId: Int = 0
    var ownerId: Int = 0
}
