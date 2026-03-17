package org.springframework.samples.petclinic.service

import org.springframework.samples.petclinic.model.User

interface UserService {
    fun saveUser(user: User)
}
