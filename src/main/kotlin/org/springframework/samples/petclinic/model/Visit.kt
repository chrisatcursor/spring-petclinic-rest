/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate

/**
 * Simple JavaBean domain object representing a visit.
 */
@Entity
@Table(name = "visits")
class Visit : BaseEntity() {

    @Column(name = "visit_date", columnDefinition = "DATE")
    var date: LocalDate = LocalDate.now()

    @field:NotEmpty
    @Column(name = "description")
    var description: String? = null

    @ManyToOne
    @JoinColumn(name = "pet_id")
    var pet: Pet? = null
}
