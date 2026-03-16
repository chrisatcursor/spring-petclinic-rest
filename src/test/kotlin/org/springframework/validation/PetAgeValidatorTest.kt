package org.springframework.validation

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.samples.petclinic.rest.validation.PetAgeValidator
import java.time.LocalDate

class PetAgeValidatorTest {
    private val validator = PetAgeValidator()

    @Test
    fun shouldReturnFalseWhenBirthDateIsInFuture() {
        val futureDate = LocalDate.now().plusDays(1)

        val context = mock(ConstraintValidatorContext::class.java)
        `when`(context.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder::class.java))

        val result = validator.isValid(futureDate, context)
        assertFalse(result)
    }

    @Test
    fun shouldReturnFalseWhenBirthDateIsOlderThan50Years() {
        val tooOldDate = LocalDate.now().minusYears(51)

        val context = mock(ConstraintValidatorContext::class.java)
        `when`(context.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder::class.java))

        val result = validator.isValid(tooOldDate, context)
        assertFalse(result)
    }

    @Test
    fun shouldReturnTrueWhenBirthDateIsValid() {
        val validDate = LocalDate.now().minusYears(10)
        val result = validator.isValid(validDate, mock(ConstraintValidatorContext::class.java))
        assertTrue(result)
    }

    @Test
    fun shouldReturnTrueWhenBirthDateIsNull() {
        val result = validator.isValid(null, mock(ConstraintValidatorContext::class.java))
        assertTrue(result)
    }
}
