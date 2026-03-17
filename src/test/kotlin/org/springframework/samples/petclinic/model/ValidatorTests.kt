package org.springframework.samples.petclinic.model

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.util.Locale

class ValidatorTests {
    private fun createValidator(): Validator {
        val localValidatorFactoryBean = LocalValidatorFactoryBean()
        localValidatorFactoryBean.afterPropertiesSet()
        return localValidatorFactoryBean
    }

    @Test
    fun shouldNotValidateWhenFirstNameEmpty() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val person = Person()
        person.firstName = ""
        person.lastName = "smith"

        val validator = createValidator()
        val constraintViolations: Set<ConstraintViolation<Person>> = validator.validate(person)

        assertThat(constraintViolations.size).isEqualTo(1)
        val violation = constraintViolations.iterator().next()
        assertThat(violation.propertyPath.toString()).isEqualTo("firstName")
        assertThat(violation.message).isEqualTo("must not be empty")
    }
}
