package io.github.guennhatking.libra_auction.mappers;

import org.mapstruct.Mapper;

import io.github.guennhatking.libra_auction.models.Permission;
import io.github.guennhatking.libra_auction.viewmodels.request.PermissionRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toEntity(PermissionRequest request);
    PermissionResponse toResponse(Permission entity);
}
