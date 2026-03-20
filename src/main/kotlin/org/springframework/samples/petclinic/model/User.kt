package org.springframework.samples.petclinic.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User {

    @Id
    @Column(name = "username")
    var username: String? = null

    @Column(name = "password")
    var password: String? = null

    @Column(name = "enabled")
    var enabled: Boolean? = null

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "user", fetch = FetchType.EAGER)
    var roles: MutableSet<Role>? = null

    @JsonIgnore
    fun addRole(roleName: String) {
        if (roles == null) {
            roles = mutableSetOf()
        }
        val role = Role().apply {
            name = roleName
        }
        roles!!.add(role)
    }
}
