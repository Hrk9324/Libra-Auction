package io.github.guennhatking.libra_auction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import io.github.guennhatking.libra_auction.dto.request.UserUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.UserResponse;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.Role;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    public abstract UserResponse toUserResponse(NguoiDung user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hoVaTen", source = "fullName")
    public abstract void updateUser(@MappingTarget NguoiDung user, UserUpdateRequest request);

}