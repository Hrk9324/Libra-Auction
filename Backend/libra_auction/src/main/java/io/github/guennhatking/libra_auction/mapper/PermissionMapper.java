package io.github.guennhatking.libra_auction.mapper;

import org.mapstruct.Mapper;

import io.github.guennhatking.libra_auction.dto.request.PermissionRequest;
import io.github.guennhatking.libra_auction.dto.response.PermissionResponse;
import io.github.guennhatking.libra_auction.models.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}