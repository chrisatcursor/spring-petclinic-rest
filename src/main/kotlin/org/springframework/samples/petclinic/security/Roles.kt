package org.springframework.samples.petclinic.security

import org.springframework.stereotype.Component

@Component("roles")
class Roles {
    val OWNER_ADMIN: String = "ROLE_OWNER_ADMIN"
    val VET_ADMIN: String = "ROLE_VET_ADMIN"
    val ADMIN: String = "ROLE_ADMIN"
}
