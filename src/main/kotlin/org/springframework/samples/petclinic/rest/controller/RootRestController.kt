package org.springframework.samples.petclinic.rest.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/"])
class RootRestController {
    @Value("#{servletContext.contextPath}")
    private lateinit var servletContextPath: String

    @RequestMapping(value = ["/"])
    @Throws(IOException::class)
    fun redirectToSwagger(response: HttpServletResponse) {
        response.sendRedirect("$servletContextPath/swagger-ui/index.html")
    }
}
