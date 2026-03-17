package org.springframework.samples.petclinic.rest.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.mapper.VetMapper
import org.springframework.samples.petclinic.model.Vet
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
class VetRestControllerTests {
    @Autowired
    private lateinit var vetRestController: VetRestController

    @Autowired
    private lateinit var vetMapper: VetMapper

    @MockitoBean
    private lateinit var clinicService: ClinicService

    private lateinit var mockMvc: MockMvc
    private lateinit var vets: MutableList<Vet>

    @BeforeEach
    fun initVets() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(vetRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        vets = mutableListOf()

        var vet = Vet()
        vet.id = 1
        vet.firstName = "James"
        vet.lastName = "Carter"
        vets.add(vet)

        vet = Vet()
        vet.id = 2
        vet.firstName = "Helen"
        vet.lastName = "Leary"
        vets.add(vet)

        vet = Vet()
        vet.id = 3
        vet.firstName = "Linda"
        vet.lastName = "Douglas"
        vets.add(vet)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetVetSuccess() {
        given(clinicService.findVetById(1)).willReturn(vets[0])
        mockMvc
            .perform(get("/api/vets/1").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"))
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetVetNotFound() {
        given(clinicService.findVetById(999)).willReturn(null)
        mockMvc
            .perform(get("/api/vets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllVetsSuccess() {
        given(clinicService.findAllVets()).willReturn(vets)
        mockMvc
            .perform(get("/api/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].firstName").value("James"))
            .andExpect(jsonPath("$.[1].id").value(2))
            .andExpect(jsonPath("$.[1].firstName").value("Helen"))
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllVetsNotFound() {
        vets.clear()
        given(clinicService.findAllVets()).willReturn(vets)
        mockMvc
            .perform(get("/api/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testCreateVetSuccess() {
        val newVet = vets[0]
        newVet.id = 999
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet))
        mockMvc
            .perform(
                post("/api/vets")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testCreateVetError() {
        val newVet = vets[0]
        newVet.id = null
        newVet.firstName = null
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet))
        mockMvc
            .perform(
                post("/api/vets")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateVetSuccess() {
        given(clinicService.findVetById(1)).willReturn(vets[0])
        val newVet = vets[0]
        newVet.firstName = "James"
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet))
        mockMvc
            .perform(
                put("/api/vets/1")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(get("/api/vets/1").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"))
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateVetError() {
        val newVet = vets[0]
        newVet.firstName = null
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet))
        mockMvc
            .perform(
                put("/api/vets/1")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteVetSuccess() {
        val newVet = vets[0]
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet))
        given(clinicService.findVetById(1)).willReturn(vets[0])
        mockMvc
            .perform(
                delete("/api/vets/1")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["VET_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteVetError() {
        val newVet = vets[0]
        val mapper = ObjectMapper()
        val newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet))
        given(clinicService.findVetById(999)).willReturn(null)
        mockMvc
            .perform(
                delete("/api/vets/999")
                    .content(newVetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNotFound)
    }
}
