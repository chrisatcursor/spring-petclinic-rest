package org.springframework.samples.petclinic.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("jpa")
class JpaUserRepositoryImpl : UserRepository {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Throws(DataAccessException::class)
    override fun save(user: User) {
        if (em.find(User::class.java, user.username) == null) {
            em.persist(user)
        } else {
            em.merge(user)
        }
    }
}
