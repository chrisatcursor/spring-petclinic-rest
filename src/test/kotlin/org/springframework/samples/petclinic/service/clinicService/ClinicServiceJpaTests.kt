package org.springframework.samples.petclinic.service.clinicService

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("jpa", "hsqldb")
class ClinicServiceJpaTests : AbstractClinicServiceTests() {
    @Autowired
    private lateinit var entityManager: EntityManager

    override fun clearCache() {
        entityManager.clear()
    }
}
