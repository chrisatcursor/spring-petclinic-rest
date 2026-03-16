package org.springframework.samples.petclinic.rest.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.mapper.SpecialtyMapper
import org.springframework.samples.petclinic.model.Specialty
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@ContextConfiguration(classes = [ApplicationTestConfig::class])
@WebAppConfiguration
class SpecialtyRestControllerTests {
    @Autowired
    private lateinit var specialtyRestController: SpecialtyRestController

    @Autowired
    private lateinit var specialtyMapper: SpecialtyMapper

    @MockitoBean
    private lateinit var clinicService: ClinicService

    private lateinit var mockMvc: MockMvc
    private lateinit var specialties: MutableList<Specialty>

    @BeforeEach
    fun initSpecialtys() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(specialtyRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        specialties = mutableListOf()

        var specialty = Specialty()
        specialty.id = 1
        specialty.name = "radiology"
        specialties.add(specialty)

        specialty = Specialty()
        specialty.id = 2
        specialty.name = "surgery"
        specialties.add(specialty)

        specialty = Specialty()
        specialty.id = 3
        specialty.name = "dentistry"
        specialties.add(specialty)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetSpecialtySuccess() {
        given(clinicService.findSpecialtyById(1)).willReturn(specialties[0])
        mockMvc
            .perform(get("/api/specialties/1").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("radiology"))
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetSpecialtyNotFound() {
        given(clinicService.findSpecialtyById(999)).willReturn(null)
        mockMvc
            .perform(get("/api/specialties/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllSpecialtysSuccess() {
        specialties.removeAt(0)
        given(clinicService.findAllSpecialties()).willReturn(specialties)
        mockMvc
            .perform(get("/api/specialties").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].name").value("surgery"))
            .andExpect(jsonPath("$.[1].id").value(3))
            .andExpect(jsonPath("$.[1].name").value("dentistry"))
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllSpecialtysNotFound() {
        specialties.clear()
        given(clinicService.findAllSpecialties()).willReturn(specialties)
        mockMvc
            .perform(get("/api/specialties").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testCreateSpecialtySuccess() {
        val newSpecialty = specialties[0]
        newSpecialty.id = 999
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty))
        mockMvc
            .perform(
                post("/api/specialties")
                    .content(newSpecialtyAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testCreateSpecialtyError() {
        val newSpecialty = specialties[0]
        newSpecialty.id = null
        newSpecialty.name = null
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty))
        mockMvc
            .perform(
                post("/api/specialties")
                    .content(newSpecialtyAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateSpecialtySuccess() {
        given(clinicService.findSpecialtyById(2)).willReturn(specialties[1])
        val newSpecialty = specialties[1]
        newSpecialty.name = "surgery I"
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty))
        mockMvc
            .perform(
                put("/api/specialties/2")
                    .content(newSpecialtyAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(get("/api/specialties/2").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("surgery I"))
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateSpecialtyError() {
        val newSpecialty = specialties[0]
        newSpecialty.name = ""
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty))
        mockMvc
            .perform(
                put("/api/specialties/1")
                    .content(newSpecialtyAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteSpecialtySuccess() {
        val newSpecialty = specialties[0]
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty))
        given(clinicService.findSpecialtyById(1)).willReturn(specialties[0])
        mockMvc
            .perform(
                delete("/api/specialties/1")
                    .content(newSpecialtyAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteSpecialtyError() {
        val newSpecialty = specialties[0]
        val mapper = ObjectMapper()
        val newSpecialtyAsJSON = mapper.writeValueAsString(specialtyMapper.toSpecialtyDto(newSpecialty))
        given(clinicService.findSpecialtyById(999)).willReturn(null)
        mockMvc
            .perform(
                delete("/api/specialties/999")
                    .content(newSpecialtyAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNotFound)
    }
}
