package org.springframework.samples.petclinic.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(name = ["petclinic.security.enable"], havingValue = "false")
class DisableSecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf(AbstractHttpConfigurer<*, *>::disable)
            .authorizeHttpRequests { authz -> authz.anyRequest().permitAll() }
            .headers { headers -> headers.frameOptions { frameOptions -> frameOptions.sameOrigin() } }
        return http.build()
    }
}
