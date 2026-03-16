package org.springframework.samples.petclinic.rest.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.mapper.UserMapper
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice
import org.springframework.samples.petclinic.service.UserService
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@SpringJUnitConfig(classes = [ApplicationTestConfig::class])
@ExtendWith(MockitoExtension::class)
@WebAppConfiguration
class UserRestControllerTests {
    @Mock
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userMapper: UserMapper

    @Autowired
    private lateinit var userRestController: UserRestController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun initVets() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(userRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Throws(Exception::class)
    fun testCreateUserSuccess() {
        val user = User()
        user.username = "username"
        user.password = "password"
        user.enabled = true
        user.addRole("OWNER_ADMIN")
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(userMapper.toUserDto(user))
        mockMvc
            .perform(
                post("/api/users")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Throws(Exception::class)
    fun testCreateUserError() {
        val user = User()
        user.username = ""
        user.password = "password"
        user.enabled = true
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(userMapper.toUserDto(user))
        mockMvc
            .perform(
                post("/api/users")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }
}
