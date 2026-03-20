package org.springframework.samples.petclinic.mapper

import org.springframework.samples.petclinic.model.Role
import org.springframework.samples.petclinic.model.User
import org.springframework.samples.petclinic.rest.dto.RoleDto
import org.springframework.samples.petclinic.rest.dto.UserDto
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toRole(roleDto: RoleDto): Role = Role().apply {
        name = roleDto.name
    }

    fun toRoleDto(role: Role): RoleDto = RoleDto().apply {
        name = role.name
    }

    fun toRoleDtos(roles: Collection<Role>): Collection<RoleDto> =
        roles.map { toRoleDto(it) }

    fun toUser(userDto: UserDto): User = User().apply {
        username = userDto.username
        password = userDto.password
        enabled = userDto.enabled
        roles = userDto.roles?.map { toRole(it) }?.toMutableSet()
    }

    fun toUserDto(user: User): UserDto = UserDto().apply {
        username = user.username
        password = user.password
        enabled = user.enabled
        user.roles?.map { toRoleDto(it) }?.let { setRoles(it) }
    }

    fun toRoles(roleDtos: Collection<RoleDto>): Collection<Role> =
        roleDtos.map { toRole(it) }
}
