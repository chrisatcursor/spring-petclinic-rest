package org.springframework.samples.petclinic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class PetClinicApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
    runApplication<PetClinicApplication>(*args)
}
