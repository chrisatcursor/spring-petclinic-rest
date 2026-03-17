package org.springframework.samples.petclinic.rest.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.samples.petclinic.mapper.OwnerMapper
import org.springframework.samples.petclinic.mapper.PetMapper
import org.springframework.samples.petclinic.mapper.VisitMapper
import org.springframework.samples.petclinic.model.Owner
import org.springframework.samples.petclinic.model.Pet
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice
import org.springframework.samples.petclinic.rest.dto.OwnerDto
import org.springframework.samples.petclinic.rest.dto.PetDto
import org.springframework.samples.petclinic.rest.dto.PetTypeDto
import org.springframework.samples.petclinic.rest.dto.VisitDto
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.text.SimpleDateFormat
import java.time.LocalDate

@SpringBootTest
@ContextConfiguration(classes = [ApplicationTestConfig::class])
@WebAppConfiguration
class OwnerRestControllerTests {
    @Autowired
    private lateinit var ownerRestController: OwnerRestController

    @Autowired
    private lateinit var ownerMapper: OwnerMapper

    @Autowired
    private lateinit var petMapper: PetMapper

    @Autowired
    private lateinit var visitMapper: VisitMapper

    @MockitoBean
    private lateinit var clinicService: ClinicService

    private lateinit var mockMvc: MockMvc
    private lateinit var owners: MutableList<OwnerDto>
    private lateinit var pets: MutableList<PetDto>
    private lateinit var visits: MutableList<VisitDto>

    @BeforeEach
    fun initOwners() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(ownerRestController)
                .setControllerAdvice(ExceptionControllerAdvice())
                .build()
        owners = mutableListOf()

        val ownerWithPet = OwnerDto()
        owners.add(
            ownerWithPet
                .id(1)
                .firstName("George")
                .lastName("Franklin")
                .address("110 W. Liberty St.")
                .city("Madison")
                .telephone("6085551023")
                .addPetsItem(getTestPetWithIdAndName(1, "Rosy"))
        )
        var owner = OwnerDto()
        owners.add(
            owner
                .id(2)
                .firstName("Betty")
                .lastName("Davis")
                .address("638 Cardinal Ave.")
                .city("Sun Prairie")
                .telephone("6085551749")
        )
        owner = OwnerDto()
        owners.add(
            owner
                .id(3)
                .firstName("Eduardo")
                .lastName("Rodriquez")
                .address("2693 Commerce St.")
                .city("McFarland")
                .telephone("6085558763")
        )
        owner = OwnerDto()
        owners.add(
            owner
                .id(4)
                .firstName("Harold")
                .lastName("Davis")
                .address("563 Friendly St.")
                .city("Windsor")
                .telephone("6085553198")
        )

        val petType = PetTypeDto()
        petType.id(2).name("dog")

        pets = mutableListOf()
        var pet = PetDto()
        pets.add(pet.id(3).name("Rosy").birthDate(LocalDate.now()).type(petType))

        pet = PetDto()
        pets.add(pet.id(4).name("Jewel").birthDate(LocalDate.now()).type(petType))

        visits = mutableListOf()
        var visit = VisitDto()
        visit.id = 2
        visit.petId = pet.id
        visit.date = LocalDate.now()
        visit.description = "rabies shot"
        visits.add(visit)

        visit = VisitDto()
        visit.id = 3
        visit.petId = pet.id
        visit.date = LocalDate.now()
        visit.description = "neutered"
        visits.add(visit)
    }

    private fun getTestPetWithIdAndName(id: Int, name: String): PetDto {
        val petType = PetTypeDto()
        val pet = PetDto()
        pet
            .id(id)
            .name(name)
            .birthDate(LocalDate.now())
            .type(petType.id(2).name("dog"))
            .addVisitsItem(getTestVisitForPet(1))
        return pet
    }

    private fun getTestVisitForPet(id: Int): VisitDto = VisitDto().id(id).date(LocalDate.now()).description("test$id")

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnerSuccess() {
        given(clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners[0]))
        mockMvc
            .perform(get("/api/owners/1").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("George"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnerNotFound() {
        given(clinicService.findOwnerById(2)).willReturn(null)
        mockMvc
            .perform(get("/api/owners/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnersListSuccess() {
        owners.removeAt(0)
        owners.removeAt(1)
        given(clinicService.findOwnerByLastName("Davis")).willReturn(ownerMapper.toOwners(owners))
        mockMvc
            .perform(get("/api/owners?lastName=Davis").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].firstName").value("Betty"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].firstName").value("Harold"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnersListNotFound() {
        owners.clear()
        given(clinicService.findOwnerByLastName("0")).willReturn(ownerMapper.toOwners(owners))
        mockMvc
            .perform(get("/api/owners?lastName=0").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllOwnersSuccess() {
        owners.removeAt(0)
        owners.removeAt(1)
        given(clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners))
        mockMvc
            .perform(get("/api/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].firstName").value("Betty"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].firstName").value("Harold"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetAllOwnersNotFound() {
        owners.clear()
        given(clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners))
        mockMvc
            .perform(get("/api/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreateOwnerSuccess() {
        val newOwnerDto = owners[0]
        newOwnerDto.id = null
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto)
        mockMvc
            .perform(
                post("/api/owners")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreateOwnerError() {
        val newOwnerDto = owners[0]
        newOwnerDto.id = null
        newOwnerDto.firstName = null
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto)
        mockMvc
            .perform(
                post("/api/owners")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateOwnerSuccess() {
        given(clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners[0]))
        val ownerId = owners[0].id
        val updatedOwnerDto = OwnerDto()
        updatedOwnerDto.id = ownerId
        updatedOwnerDto.firstName = "GeorgeI"
        updatedOwnerDto.lastName = "Franklin"
        updatedOwnerDto.address = "110 W. Liberty St."
        updatedOwnerDto.city = "Madison"
        updatedOwnerDto.telephone = "6085551023"
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto)
        mockMvc
            .perform(
                put("/api/owners/$ownerId")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/owners/$ownerId")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.firstName").value("GeorgeI"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateOwnerSuccessNoBodyId() {
        given(clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners[0]))
        val ownerId = owners[0].id
        val updatedOwnerDto = OwnerDto()
        updatedOwnerDto.firstName = "GeorgeI"
        updatedOwnerDto.lastName = "Franklin"
        updatedOwnerDto.address = "110 W. Liberty St."
        updatedOwnerDto.city = "Madison"
        updatedOwnerDto.telephone = "6085551023"
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto)
        mockMvc
            .perform(
                put("/api/owners/$ownerId")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/owners/$ownerId")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.firstName").value("GeorgeI"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateOwnerError() {
        val newOwnerDto = owners[0]
        newOwnerDto.firstName = ""
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto)
        mockMvc
            .perform(
                put("/api/owners/1")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteOwnerSuccess() {
        val newOwnerDto = owners[0]
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto)
        val owner: Owner = ownerMapper.toOwner(owners[0])
        given(clinicService.findOwnerById(1)).willReturn(owner)
        mockMvc
            .perform(
                delete("/api/owners/1")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testDeleteOwnerError() {
        val newOwnerDto = owners[0]
        val mapper = ObjectMapper()
        val newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto)
        given(clinicService.findOwnerById(999)).willReturn(null)
        mockMvc
            .perform(
                delete("/api/owners/999")
                    .content(newOwnerAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreatePetSuccess() {
        val owner: Owner = ownerMapper.toOwner(owners[0])
        given(clinicService.findOwnerById(1)).willReturn(owner)
        val newPet = pets[0]
        newPet.id = 999
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val newPetAsJSON = mapper.writeValueAsString(newPet)
        mockMvc
            .perform(
                post("/api/owners/1/pets")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreatePetError() {
        val newPet = pets[0]
        newPet.id = null
        newPet.name = null
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val newPetAsJSON = mapper.writeValueAsString(newPet)
        mockMvc
            .perform(
                post("/api/owners/1/pets")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreatePetShouldNotExposeTechnicalDetails() {
        val newPet = pets[0]
        newPet.id = null
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val newPetAsJSON = mapper.writeValueAsString(newPet)
        val technicalMessage = "could not execute statement; SQL [insert into pets ...]; constraint [fk_pet_owner]"
        given(clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners[0]))
        doThrow(DataIntegrityViolationException(technicalMessage)).`when`(clinicService).savePet(any(Pet::class.java) ?: Pet())
        mockMvc
            .perform(
                post("/api/owners/1/pets")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Request could not be processed"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreatePetWithUnknownOwnerShouldReturnNotFound() {
        val newPet = pets[0]
        newPet.id = null
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val newPetAsJSON = mapper.writeValueAsString(newPet)
        given(clinicService.findOwnerById(1000000)).willReturn(null)
        mockMvc
            .perform(
                post("/api/owners/1000000/pets")
                    .content(newPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testCreateVisitSuccess() {
        val newVisit = visits[0]
        newVisit.id = 999
        val mapper = ObjectMapper()
        val newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisit(newVisit))
        mockMvc
            .perform(
                post("/api/owners/1/pets/1/visits")
                    .content(newVisitAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isCreated)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnerPetSuccess() {
        val owner = ownerMapper.toOwner(owners[0])
        given(clinicService.findOwnerById(2)).willReturn(owner)
        val pet = petMapper.toPet(pets[0])
        pet.owner = owner
        given(clinicService.findPetById(1)).willReturn(pet)
        mockMvc
            .perform(get("/api/owners/2/pets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnersPetsWithOwnerNotFound() {
        owners.clear()
        given(clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners))
        mockMvc
            .perform(get("/api/owners/1/pets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testGetOwnersPetsWithPetNotFound() {
        val owner1 = ownerMapper.toOwner(owners[0])
        given(clinicService.findOwnerById(1)).willReturn(owner1)
        mockMvc
            .perform(get("/api/owners/1/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateOwnersPetSuccess() {
        val ownerId = owners[0].id!!
        val petId = pets[0].id!!
        given(clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(owners[0]))
        given(clinicService.findPetById(petId)).willReturn(petMapper.toPet(pets[0]))
        val updatedPetDto = pets[0]
        updatedPetDto.name = "Rex"
        updatedPetDto.birthDate = LocalDate.of(2020, 1, 15)
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val updatedPetAsJSON = mapper.writeValueAsString(updatedPetDto)
        mockMvc
            .perform(
                put("/api/owners/$ownerId/pets/$petId")
                    .content(updatedPetAsJSON)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            ).andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateOwnersPetOwnerNotFound() {
        val ownerId = 0
        val petId = pets[0].id!!
        given(clinicService.findOwnerById(ownerId)).willReturn(null)
        val petDto = pets[0]
        petDto.name = "Thor"
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val updatedPetAsJSON = mapper.writeValueAsString(petDto)
        mockMvc
            .perform(
                put("/api/owners/$ownerId/pets/$petId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatedPetAsJSON)
            ).andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(roles = ["OWNER_ADMIN"])
    @Throws(Exception::class)
    fun testUpdateOwnersPetPetNotFound() {
        val ownerId = owners[0].id!!
        val petId = 0
        given(clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(owners[0]))
        given(clinicService.findPetById(petId)).willReturn(null)
        val petDto = pets[0]
        petDto.name = "Ghost"
        petDto.birthDate = LocalDate.of(2020, 1, 1)
        val mapper: ObjectMapper = JsonMapper.builder().defaultDateFormat(SimpleDateFormat("dd/MM/yyyy")).build()
        val updatedPetAsJSON = mapper.writeValueAsString(petDto)
        mockMvc
            .perform(
                put("/api/owners/$ownerId/pets/$petId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatedPetAsJSON)
            ).andExpect(status().isNotFound)
    }
}
