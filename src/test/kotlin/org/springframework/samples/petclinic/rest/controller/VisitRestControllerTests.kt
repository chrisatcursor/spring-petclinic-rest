package org.springframework.samples.petclinic.rest.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.mapper.VisitMapper
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.model.PetType
import org.springframework.samples.petclinic.model.Visit
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
import java.time.LocalDate

@SpringBootTest
@ContextConfiguration(classes = [ApplicationTestConfig::class])
@WebAppConfiguration
class VisitRestControllerTests {
    @Autowired
    private lateinit var visitRestController: VisitRestController

    @MockitoBean
    private lateinit var clinicService: ClinicService

    @Autowired
    private lateinit var visitMapper: VisitMapper

    private lateinit var mockMvc: MockMvc
    private lateinit var visits: MutableList<Visit>

    @BeforeEach
    fun initVisits() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(visitRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()

        visits = mutableListOf()

        val owner = Owner()
        owner.id = 1
        owner.firstName = "Eduardo"
        owner.lastName = "Rodriquez"
        owner.address = "2693 Commerce St."
        owner.city = "McFarland"
        owner.telephone = "6085558763"

        val petType = PetType()
        petType.id = 2
        petType.name = "dog"

        val pet = Pet()
        pet.id = 8
        pet.name = "Rosy"
        pet.birthDate = LocalDate.now()
        pet.owner = owner
        pet.type = petType

        var visit = Visit()
        visit.id = 2
        visit.pet = pet
        visit.date = LocalDate.now()
        visit.description = "rabies shot"
        visits.add(visit)

        visit = Visit()
        visit.id = 3
        visit.pet = pet
        visit.date = LocalDate.now()
        visit.description = "neutered"
        visits.add(visit)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetVisitSuccess() {
        given(clinicService.findVisitById(2)).willReturn(visits[0])
        mockMvc
            .perform(get("/api/visits/2").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetVisitNotFound() {
        given(clinicService.findVisitById(999)).willReturn(null)
        mockMvc
            .perform(get("/api/visits/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllVisitsSuccess() {
        given(clinicService.findAllVisits()).willReturn(visits)
        mockMvc
            .perform(get("/api/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].description").value("rabies shot"))
            .andExpect(jsonPath("$.[1].id").value(3))
            .andExpect(jsonPath("$.[1].description").value("neutered"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllVisitsNotFound() {
        visits.clear()
        given(clinicService.findAllVisits()).willReturn(visits)
        mockMvc
            .perform(get("/api/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreateVisitSuccess() {
        val newVisit = visits[0]
        newVisit.id = 999
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit))
        mockMvc
            .perform(
                post("/api/visits")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreateVisitError() {
        val newVisit = visits[0]
        newVisit.id = null
        newVisit.description = null
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit))
        mockMvc
            .perform(
                post("/api/visits")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateVisitSuccess() {
        given(clinicService.findVisitById(2)).willReturn(visits[0])
        val newVisit = visits[0]
        newVisit.description = "rabies shot test"
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit))
        mockMvc
            .perform(
                put("/api/visits/2")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(get("/api/visits/2").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot test"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateVisitError() {
        val newVisit = visits[0]
        newVisit.description = null
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit))
        mockMvc
            .perform(
                put("/api/visits/2")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteVisitSuccess() {
        val newVisit = visits[0]
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit))
        given(clinicService.findVisitById(2)).willReturn(visits[0])
        mockMvc
            .perform(
                delete("/api/visits/2")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteVisitError() {
        val newVisit = visits[0]
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit))
        given(clinicService.findVisitById(999)).willReturn(null)
        mockMvc
            .perform(
                delete("/api/visits/999")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNotFound)
    }
}
