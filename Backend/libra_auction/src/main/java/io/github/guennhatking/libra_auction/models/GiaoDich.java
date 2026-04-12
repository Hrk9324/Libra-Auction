package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.enums.Enums.TinhTrangGiaoDich;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GiaoDich {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private Enums.LoaiGiaoDich loaiGiaoDich;

    private long soTien;
    private LocalDateTime ngayTao;

    @Enumerated(EnumType.STRING)
    private TinhTrangGiaoDich tinhTrangGiaoDich = TinhTrangGiaoDich.DANG_XU_LY;

    private String maGiaoDichCuaDoiTac; // Lưu lại mã giao dịch của VNPay

    // CONSTRUCTOR
    protected GiaoDich() {
    }

    public GiaoDich(Enums.LoaiGiaoDich loaiGiaoDich, long soTien) {
        this.loaiGiaoDich = loaiGiaoDich;
        this.soTien = soTien;
        this.ngayTao = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public Enums.LoaiGiaoDich getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public long getSoTien() {
        return soTien;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public TinhTrangGiaoDich geTinhTrangGiaoDich() {
        return tinhTrangGiaoDich;
    }

    public String getMaGiaoDichCuaDoiTac() {
        return maGiaoDichCuaDoiTac;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setLoaiGiaoDich(Enums.LoaiGiaoDich loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public void setSoTien(long soTien) {
        this.soTien = soTien;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public void setTinhTrangGiaoDich(TinhTrangGiaoDich tinhTrangGiaoDich) {
        this.tinhTrangGiaoDich = tinhTrangGiaoDich;
    }

    public void setMaGiaoDichCuaDoiTac(String maGiaoDichCuaDoiTac) {
        this.maGiaoDichCuaDoiTac = maGiaoDichCuaDoiTac;
    }
}