package org.springframework.samples.petclinic.repository.jdbc

import org.springframework.data.jdbc.core.OneToManyResultSetExtractor
import org.springframework.samples.petclinic.model.Visit
import java.sql.ResultSet
import java.sql.SQLException

class JdbcPetVisitExtractor : OneToManyResultSetExtractor<JdbcPet, Visit, Int>(JdbcPetRowMapper(), JdbcVisitRowMapper()) {
    @Throws(SQLException::class)
    override fun mapPrimaryKey(rs: ResultSet): Int = rs.getInt("pets_id")

    @Throws(SQLException::class)
    override fun mapForeignKey(rs: ResultSet): Int? =
        if (rs.getObject("visits_pet_id") == null) {
            null
        } else {
            rs.getInt("visits_pet_id")
        }

    override fun addChild(root: JdbcPet, child: Visit) {
        root.addVisit(child)
    }
}
