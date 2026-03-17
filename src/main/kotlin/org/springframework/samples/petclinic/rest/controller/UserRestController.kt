package org.springframework.samples.petclinic.rest.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.mapper.UserMapper
import org.springframework.samples.petclinic.rest.api.UsersApi
import org.springframework.samples.petclinic.rest.dto.UserDto
import org.springframework.samples.petclinic.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(exposedHeaders = ["errors", "content-type"])
@RequestMapping(value = ["/api"])
class UserRestController(
    private val userService: UserService,
    private val userMapper: UserMapper
) : UsersApi {
    @PreAuthorize("hasRole(@roles.ADMIN)")
    override fun addUser(userDto: UserDto): ResponseEntity<UserDto> {
        val headers = HttpHeaders()
        val user = userMapper.toUser(userDto)
        userService.saveUser(user)
        return ResponseEntity(userMapper.toUserDto(user), headers, HttpStatus.CREATED)
    }
}
