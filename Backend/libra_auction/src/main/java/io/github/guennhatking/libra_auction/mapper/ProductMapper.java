package io.github.guennhatking.libra_auction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.github.guennhatking.libra_auction.dto.request.ProductCreateRequest;
import io.github.guennhatking.libra_auction.dto.request.ProductUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.ProductResponse;
import io.github.guennhatking.libra_auction.models.TaiSan;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "danhMuc", ignore = true)
    @Mapping(target = "thongTinPhienDauGia", ignore = true)
    TaiSan toProduct(ProductCreateRequest request);

    @Mapping(target = "danhMucId", source = "danhMuc.id")
    @Mapping(target = "danhMucName", source = "danhMuc.tenDanhMuc")
    ProductResponse toProductResponse(TaiSan taiSan);
}
