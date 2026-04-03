package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class YeuCau {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    @ManyToOne
    protected NguoiDung nguoiDung;

    @ManyToOne
    protected ThongBao thongBao;

    protected String token;

    @Enumerated(EnumType.STRING)
    protected Enums.LoaiYeuCau loaiYeuCau;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiYeuCau trangThaiYeuCau;

    protected LocalDateTime thoiGianHetHanKichHoat;
    protected LocalDateTime thoiGianHetHanSuDung;

    // CONSTRUCTOR
    protected YeuCau() {
    }

    public YeuCau(NguoiDung nguoiDung, Enums.LoaiYeuCau loaiYeuCau) {
        this.nguoiDung = nguoiDung;
        this.loaiYeuCau = loaiYeuCau;
        this.trangThaiYeuCau = Enums.TrangThaiYeuCau.KHOI_TAO;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public ThongBao getThongBao() {
        return thongBao;
    }

    public String getToken() {
        return token;
    }

    public Enums.LoaiYeuCau getLoaiYeuCau() {
        return loaiYeuCau;
    }

    public Enums.TrangThaiYeuCau getTrangThaiYeuCau() {
        return trangThaiYeuCau;
    }

    public LocalDateTime getThoiGianHetHanKichHoat() {
        return thoiGianHetHanKichHoat;
    }

    public LocalDateTime getThoiGianHetHanSuDung() {
        return thoiGianHetHanSuDung;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public void setThongBao(ThongBao thongBao) {
        this.thongBao = thongBao;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLoaiYeuCau(Enums.LoaiYeuCau loaiYeuCau) {
        this.loaiYeuCau = loaiYeuCau;
    }

    public void setTrangThaiYeuCau(Enums.TrangThaiYeuCau trangThaiYeuCau) {
        this.trangThaiYeuCau = trangThaiYeuCau;
    }

    public void setThoiGianHetHanKichHoat(LocalDateTime thoiGianHetHanKichHoat) {
        this.thoiGianHetHanKichHoat = thoiGianHetHanKichHoat;
    }

    public void setThoiGianHetHanSuDung(LocalDateTime thoiGianHetHanSuDung) {
        this.thoiGianHetHanSuDung = thoiGianHetHanSuDung;
    }
}
