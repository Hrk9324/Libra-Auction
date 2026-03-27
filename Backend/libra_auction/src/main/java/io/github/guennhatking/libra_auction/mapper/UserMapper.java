package io.github.guennhatking.libra_auction.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import io.github.guennhatking.libra_auction.dto.request.UserCreationRequest;
import io.github.guennhatking.libra_auction.dto.request.UserUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.UserResponse;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.Role;


@Mapper(componentModel = "spring")
public abstract class UserMapper {

    public abstract NguoiDung toUser(UserCreationRequest request);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    public abstract UserResponse toUserResponse(NguoiDung user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleNamesToRoles")
    public abstract void updateUser(@MappingTarget NguoiDung user, UserUpdateRequest request);

    // Helper methods for transforming roles
    @Named("rolesToRoleNames")
    public Set<String> rolesToRoleNames(List<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("roleNamesToRoles")
    public List<Role> roleNamesToRoles(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return List.of();
        }
        return roleNames.stream()
                .map(name -> new Role(name, null))
                .collect(Collectors.toList());
    }
}