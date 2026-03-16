package org.springframework.samples.petclinic.service.userService

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.service.UserService

abstract class AbstractUserServiceTests {
    @Autowired
    private lateinit var userService: UserService

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun shouldAddUser() {
        val user = User()
        user.username = "username"
        user.password = "password"
        user.enabled = true
        user.addRole("OWNER_ADMIN")

        userService.saveUser(user)
        assertThat(user.roles!!.parallelStream().allMatch { role -> role.name!!.startsWith("ROLE_") }, `is`(true))
        assertThat(user.roles!!.parallelStream().allMatch { role -> role.user != null }, `is`(true))
    }
}
