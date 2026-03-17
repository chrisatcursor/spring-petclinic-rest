package org.springframework.samples.petclinic.service.clinicService

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("hsqldb", "jdbc")
@TestPropertySource(properties = ["spring.sql.init.platform=hsqldb"])
class ClinicServiceHsqlJdbcTests : AbstractClinicServiceTests()
