package io.github.guennhatking.libra_auction.mappers;

import org.mapstruct.Mapper;

import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.viewmodels.request.UserUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    NguoiDung toEntity(UserUpdateRequest request);
    UserResponse toResponse(NguoiDung entity);
}
