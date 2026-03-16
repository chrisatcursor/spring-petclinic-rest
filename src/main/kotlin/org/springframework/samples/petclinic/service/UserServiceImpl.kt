package org.springframework.samples.petclinic.service

import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    @Transactional
    override fun saveUser(user: User) {
        if (user.roles.isNullOrEmpty()) {
            throw IllegalArgumentException("User must have at least a role set!")
        }

        for (role in user.roles.orEmpty()) {
            if (role.name?.startsWith("ROLE_") != true) {
                role.name = "ROLE_${role.name}"
            }

            if (role.user == null) {
                role.user = user
            }
        }

        userRepository.save(user)
    }
}
