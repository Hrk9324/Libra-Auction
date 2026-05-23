package io.github.guennhatking.libra_auction.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.models.product.TaiSan;
import io.github.guennhatking.libra_auction.viewmodels.response.AttributeResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;

@Mapper(componentModel = "spring", uses = { ProductImageMapper.class })
public interface AuctionMapper {

    @Mapping(source = "taiSan.danhMuc.id", target = "category_id", defaultValue = "uncategorized")
    @Mapping(source = "taiSan.danhMuc.tenDanhMuc", target = "category_name")
    @Mapping(source = "id", target = "auction_id")

    @Mapping(source = "trangThaiPhien", target = "auction_status", defaultValue = "CHUA_BAT_DAU")
    @Mapping(source = "trangThaiKiemDuyet", target = "approval_status")
    @Mapping(source = "thoiGianBatDau", target = "start_time")
    @Mapping(source = "thoiLuong", target = "duration")
    @Mapping(source = "giaKhoiDiem", target = "starting_price")
    @Mapping(source = "buocGiaNhoNhat", target = "min_bid_increment")
    @Mapping(source = "giaKhoiDiem", target = "current_price")

    @Mapping(source = "taiSan.id", target = "product_id")
    @Mapping(source = "taiSan.tenTaiSan", target = "product_name")
    @Mapping(source = "taiSan.soLuong", target = "quantity")
    @Mapping(source = "taiSan.moTa", target = "description")

    @Mapping(source = "taiSan.hinhAnhTaiSanList", target = "images")
    @Mapping(source = ".", target = "auction_name", qualifiedByName = "resolveAuctionTitle")
    @Mapping(source = ".", target = "attributes", qualifiedByName = "resolveAttributes")
    @Mapping(source = ".", target = "total_bids", qualifiedByName = "resolveTotalBids")
    @Mapping(source = ".", target = "total_participants", qualifiedByName = "resolveTotalParticipants")
    AuctionResponse toAuctionResponse(PhienDauGia session);

    List<AuctionResponse> toAuctionResponseList(List<PhienDauGia> sessions);

    @Named("resolveAttributes")
    default List<AttributeResponse> resolveAttributes(PhienDauGia session) {
        if (session == null || session.getTaiSan() == null)
            return Collections.emptyList();

        List<AttributeResponse> results = new ArrayList<>();
        TaiSan taiSan = session.getTaiSan();

        // Lấy từ ThuocTinhTaiSan (isSystem = false)
        if (taiSan.getThuocTinhTaiSanList() != null) {
            taiSan.getThuocTinhTaiSanList().forEach(
                    attr -> results.add(new AttributeResponse(attr.getTenThuocTinh(), attr.getGiaTri(), false)));
        }

        // Lấy từ KetHopThuocTinh -> ThuocTinhChuanHoa (isSystem = true)
        if (taiSan.getKetHopThuocTinhs() != null) {
            taiSan.getKetHopThuocTinhs().forEach(kh -> {
                if (kh.getThuocTinhChuanHoa() != null) {
                    results.add(new AttributeResponse(
                            kh.getThuocTinhChuanHoa().getTenThuocTinh(),
                            kh.getThuocTinhChuanHoa().getGiaTri(),
                            true));
                }
            });
        }

        return results;
    }

    @Named("resolveTotalBids")
    default Long resolveTotalBids(PhienDauGia session) {
        if (session == null || session.getLichSuDatGia() == null)
            return 0L;
        return (long) session.getLichSuDatGia().size();
    }

    @Named("resolveTotalParticipants")
    default Long resolveTotalParticipants(PhienDauGia session) {
        if (session == null || session.getDanhSachThamGia() == null)
            return 0L;
        return (long) session.getDanhSachThamGia().size();
    }

    @Named("resolveAuctionTitle")
    default String resolveAuctionTitle(PhienDauGia session) {
        if (session == null || session.getTaiSan() == null) {
            return "Vật phẩm không tên";
        }

        String tenTaiSan = session.getTaiSan().getTenTaiSan();
        if (tenTaiSan != null && !tenTaiSan.trim().isEmpty()) {
            return tenTaiSan;
        }

        return "Vật phẩm không tên";
    }
}