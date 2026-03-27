package io.github.guennhatking.libra_auction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.github.guennhatking.libra_auction.dto.request.RoleRequest;
import io.github.guennhatking.libra_auction.dto.response.RoleResponse;
import io.github.guennhatking.libra_auction.models.Role;
@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}