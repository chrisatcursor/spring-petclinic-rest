package org.springframework.samples.petclinic.rest.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate

class PetAgeValidator : ConstraintValidator<PetAgeValidation, LocalDate> {
    override fun isValid(birthDate: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (birthDate == null) {
            return true
        }

        val today = LocalDate.now()

        if (birthDate.isAfter(today)) {
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate("Birth date cannot be in the future")
                .addConstraintViolation()
            return false
        }

        if (birthDate.isBefore(today.minusYears(50))) {
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate("Birth date cannot be older than 50 years")
                .addConstraintViolation()
            return false
        }

        return true
    }
}
