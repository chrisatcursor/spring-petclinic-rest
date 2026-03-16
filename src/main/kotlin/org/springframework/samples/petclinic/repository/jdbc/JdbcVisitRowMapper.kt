package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.jdbc.core.RowMapper
import org.springframework.samples.petclinic.model.Visit
import java.sql.ResultSet
import java.time.LocalDate

class JdbcVisitRowMapper : RowMapper<Visit> {
    override fun mapRow(rs: ResultSet, row: Int): Visit =
        Visit().apply {
            id = rs.getInt("visit_id")
            date = rs.getObject("visit_date", LocalDate::class.java)
            description = rs.getString("description")
        }
}
