package io.github.guennhatking.libra_auction.dto.response;

import io.github.guennhatking.libra_auction.enums.Enums;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionSessionResponse {
    String id;
    String taiSanId;
    String tenTaiSan;
    LocalDateTime thoiGianBatDau;
    long thoiLuong;
    long giaKhoiDiem;
    long buocGiaNhoNhat;
    Enums.LoaiDauGia loaiDauGia;
    Enums.TrangThaiPhien trangThaiPhien;
    Enums.TrangThaiKiemDuyet trangThaiKiemDuyet;
    LocalDateTime thoiGianTao;
    String nguoiTaoId;
    String nguoiTaoName;
}
