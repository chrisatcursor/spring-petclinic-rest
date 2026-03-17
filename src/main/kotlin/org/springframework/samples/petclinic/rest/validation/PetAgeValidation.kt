package org.springframework.samples.petclinic.rest.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PetAgeValidator::class])
@MustBeDocumented
annotation class PetAgeValidation(
    val message: String = "Birth date must not be in the future or older than 50 years",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
