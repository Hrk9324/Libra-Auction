package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public abstract class YeuCau {
    protected NguoiDung nguoiDung;
    protected ThongBao thongBao;
    protected String token;
    protected Enums.LoaiYeuCau loaiYeuCau;
    protected Enums.TrangThaiYeuCau trangThaiYeuCau;
    protected LocalDateTime thoiGianHetHanKichHoat;
    protected LocalDateTime thoiGianHetHanSuDung;

    public YeuCau(NguoiDung nguoiDung, Enums.LoaiYeuCau loaiYeuCau) {
        this.nguoiDung = nguoiDung;
        this.loaiYeuCau = loaiYeuCau;
        this.trangThaiYeuCau = Enums.TrangThaiYeuCau.KHOI_TAO;
    }

    public abstract void kichHoat();

    public abstract void suDung();
}
