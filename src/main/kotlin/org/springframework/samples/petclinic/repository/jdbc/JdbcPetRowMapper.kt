package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDate

class JdbcPetRowMapper : RowMapper<JdbcPet> {
    override fun mapRow(rs: ResultSet, rownum: Int): JdbcPet =
        JdbcPet().apply {
            id = rs.getInt("pets_id")
            name = rs.getString("name")
            birthDate = rs.getObject("birth_date", LocalDate::class.java)
            typeId = rs.getInt("type_id")
            ownerId = rs.getInt("owner_id")
        }
}
