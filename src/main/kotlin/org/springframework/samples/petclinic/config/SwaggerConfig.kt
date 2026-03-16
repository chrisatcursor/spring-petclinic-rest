package org.springframework.samples.petclinic.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Collections

@Configuration
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .components(Components())
            .info(
                Info()
                    .title("REST Petclinic backend API documentation")
                    .version("1.0")
                    .termsOfService("https://github.com/spring-petclinic/spring-petclinic-rest/blob/master/terms.txt")
                    .description(
                        "This is the REST API documentation of the Spring Petclinic backend. " +
                            "If authentication is enabled, use admin/admin when calling the APIs"
                    )
                    .license(swaggerLicense())
                    .contact(swaggerContact())
            )

    private fun swaggerContact(): Contact =
        Contact()
            .name("Vitaliy Fedoriv")
            .email("vitaliy.fedoriv@gmail.com")
            .url("https://github.com/spring-petclinic/spring-petclinic-rest")

    private fun swaggerLicense(): License =
        License()
            .name("Apache 2.0")
            .url("http://www.apache.org/licenses/LICENSE-2.0")
            .extensions(Collections.emptyMap())
}
