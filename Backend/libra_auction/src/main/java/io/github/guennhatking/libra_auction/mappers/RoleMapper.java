package io.github.guennhatking.libra_auction.mappers;

import org.mapstruct.Mapper;

import io.github.guennhatking.libra_auction.models.Role;
import io.github.guennhatking.libra_auction.viewmodels.request.RoleRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toEntity(RoleRequest request);
    RoleResponse toResponse(Role entity);
}
