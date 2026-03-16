package org.springframework.samples.petclinic.rest.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.mapper.PetMapper
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice
import org.springframework.samples.petclinic.rest.dto.OwnerDto
import org.springframework.samples.petclinic.rest.dto.PetDto
import org.springframework.samples.petclinic.rest.dto.PetTypeDto
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.text.SimpleDateFormat
import java.time.LocalDate

import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`

@SpringBootTest
@ContextConfiguration(classes = [ApplicationTestConfig::class])
@WebAppConfiguration
class PetRestControllerTests {
    @MockitoBean
    protected lateinit var clinicService: ClinicService

    @Autowired
    private lateinit var petRestController: PetRestController

    @Autowired
    private lateinit var petMapper: PetMapper

    private lateinit var mockMvc: MockMvc
    private lateinit var pets: MutableList<PetDto>

    @BeforeEach
    fun initPets() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(petRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        pets = mutableListOf()

        val owner =
            OwnerDto()
                .id(1)
                .firstName("Eduardo")
                .lastName("Rodriquez")
                .address("2693 Commerce St.")
                .city("McFarland")
                .telephone("6085558763")

        val petType = PetTypeDto().id(2).name("dog")

        var pet = PetDto().id(3).name("Rosy").birthDate(LocalDate.now()).type(petType)
        pets.add(pet)

        pet = PetDto().id(4).name("Jewel").birthDate(LocalDate.now()).type(petType)
        pets.add(pet)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetPetSuccess() {
        given(clinicService.findPetById(3)).willReturn(petMapper.toPet(pets[0]))
        mockMvc
            .perform(get("/api/pets/3").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetPetNotFound() {
        given(clinicService.findPetById(999)).willReturn(null)
        mockMvc
            .perform(get("/api/pets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllPetsSuccess() {
        val mockPets: Collection<Pet> = petMapper.toPets(pets)
        `when`(clinicService.findAllPets()).thenReturn(mockPets)

        mockMvc
            .perform(get("/api/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(3))
            .andExpect(jsonPath("$.[0].name").value("Rosy"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].name").value("Jewel"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllPetsNotFound() {
        pets.clear()
        given(clinicService.findAllPets()).willReturn(petMapper.toPets(pets))
        mockMvc
            .perform(get("/api/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdatePetSuccess() {
        given(clinicService.findPetById(3)).willReturn(petMapper.toPet(pets[0]))
        val newPet = pets[0]
        newPet.name = "Rosy I"
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()

        val newPetAsJSON = mapper.writeValueAsString(newPet)
        mockMvc
            .perform(
                put("/api/pets/3")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/pets/3")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy I"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdatePetError() {
        val newPet = pets[0]
        newPet.name = null
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val newPetAsJSON = mapper.writeValueAsString(newPet)

        mockMvc
            .perform(
                put("/api/pets/3")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testDeletePetSuccess() {
        val newPet = pets[0]
        val mapper = ObjectMapper()
        val newPetAsJSON = mapper.writeValueAsString(newPet)
        given(clinicService.findPetById(3)).willReturn(petMapper.toPet(pets[0]))
        mockMvc
            .perform(
                delete("/api/pets/3")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testDeletePetError() {
        val newPet = pets[0]
        val mapper = ObjectMapper()
        val newPetAsJSON = mapper.writeValueAsString(newPet)
        given(clinicService.findPetById(999)).willReturn(null)
        mockMvc
            .perform(
                delete("/api/pets/999")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNotFound)
    }
}
