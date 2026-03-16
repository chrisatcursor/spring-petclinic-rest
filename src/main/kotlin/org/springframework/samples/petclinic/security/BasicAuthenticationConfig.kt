package org.springframework.samples.petclinic.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(name = ["petclinic.security.enable"], havingValue = "true")
class BasicAuthenticationConfig(private val dataSource: DataSource) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf(AbstractHttpConfigurer<*, *>::disable)
            .authorizeHttpRequests { authz -> authz.anyRequest().authenticated() }
            .httpBasic(Customizer.withDefaults())
        return http.build()
    }

    @Autowired
    @Throws(Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth
            .jdbcAuthentication()
            .dataSource(dataSource)
            .usersByUsernameQuery("select username,password,enabled from users where username=?")
            .authoritiesByUsernameQuery("select username,role from roles where username=?")
    }
}
