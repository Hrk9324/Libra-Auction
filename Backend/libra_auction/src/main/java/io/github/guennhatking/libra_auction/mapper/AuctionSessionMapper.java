package io.github.guennhatking.libra_auction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.github.guennhatking.libra_auction.dto.request.AuctionSessionCreateRequest;
import io.github.guennhatking.libra_auction.dto.response.AuctionSessionResponse;
import io.github.guennhatking.libra_auction.models.PhienDauGia;

@Mapper(componentModel = "spring")
public interface AuctionSessionMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taiSan", ignore = true)
    @Mapping(target = "nguoiTao", ignore = true)
    @Mapping(target = "thongTinPhienDauGia", ignore = true)
    @Mapping(target = "thoiGianTao", ignore = true)
    @Mapping(target = "trangThaiKiemDuyet", ignore = true)
    @Mapping(target = "trangThaiPhien", ignore = true)
    @Mapping(target = "ketQuaDauGia", ignore = true)
    @Mapping(target = "danhSachCauHoi", ignore = true)
    @Mapping(target = "lichSuDatGia", ignore = true)
    @Mapping(target = "danhSachThamGia", ignore = true)
    @Mapping(target = "danhSachBoCuoc", ignore = true)
    PhienDauGia toAuctionSession(AuctionSessionCreateRequest request);

    @Mapping(target = "taiSanId", source = "taiSan.id")
    @Mapping(target = "tenTaiSan", source = "taiSan.tenTaiSan")
    @Mapping(target = "nguoiTaoId", source = "nguoiTao.id")
    @Mapping(target = "nguoiTaoName", source = "nguoiTao.hoVaTen")
    AuctionSessionResponse toAuctionSessionResponse(PhienDauGia phienDauGia);
}
